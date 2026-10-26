package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.ExDatasetAnalisisDto;
import com.example.proyectobackend.dto.ExDatasetUploadDto;
import com.example.proyectobackend.dto.OcrDatasetResponseDto;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExDatasetService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ExOcrService exOcrService;

    private static final Set<String> STOP_WORDS = Set.of(
            "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por",
            "un", "para", "con", "no", "una", "su", "al", "lo", "como", "mas", "pero",
            "sus", "le", "ya", "o", "este", "si", "porque", "esta", "son", "entre", "cuando"
    );

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F600}-\\x{1F64F}|\\x{1F300}-\\x{1F5FF}|\\x{1F680}-\\x{1F6FF}|\\x{1F1E0}-\\x{1F1FF}|\\x{2600}-\\x{26FF}|\\x{2700}-\\x{27BF}]",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    // Formato del OCR y de las exportaciones de WhatsApp:
    // [22/10/26, 7:03 p.m.] remitente: texto
    private static final Pattern LINEA_CHAT_PATTERN = Pattern.compile(
            "^\\[[^\\]]+\\]\\s*([^:]+):\\s*(.*)$"
    );

    @Transactional
    public ExDatasetAnalisisDto procesarYEntrenarDataset(String email, ExDatasetUploadDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        String rawChat = dto.getContenidoChat();
        String nombreEx = resolverNombreEx(dto.getNombreRemitenteEx(), usuario);

        ExDatasetAnalisisDto analisis = analizar(rawChat, nombreEx, usuario.getNombre());

        // Guardamos el chat completo (no un resumen) para que la IA imite el
        // estilo real con ejemplos.
        usuario.setPersonalidadEx(analisis.getTonoDetectado());
        usuario.setDatasetEntrenamientoEx(rawChat);
        usuarioRepository.save(usuario);

        log.info("Dataset del ex procesado para el usuario {}. Tono inferido: {}",
                email, analisis.getTonoDetectado());

        analisis.setMensaje("Dataset analizado y clon de personalidad actualizado exitosamente.");
        return analisis;
    }

    /**
     * Flujo nuevo: el frontend sube capturas de WhatsApp, el backend las
     * reenvía al OCR de Python y guarda el TXT resultante en
     * Usuario.datasetEntrenamientoEx.
     */
    @Transactional
    public ExDatasetAnalisisDto procesarImagenes(String email,
                                                 List<MultipartFile> imagenes,
                                                 String nombreRemitenteEx) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        String nombreEx = resolverNombreEx(nombreRemitenteEx, usuario);

        OcrDatasetResponseDto ocr = exOcrService.procesar(
                usuario.getId(),
                nombreEx,
                usuario.getDatasetEntrenamientoEx(),
                imagenes
        );

        String texto = ocr.getTexto();
        ExDatasetAnalisisDto analisis = analizar(texto, nombreEx, usuario.getNombre());

        usuario.setNombreEx(nombreEx);
        usuario.setPersonalidadEx(analisis.getTonoDetectado());
        usuario.setDatasetEntrenamientoEx(texto);
        usuarioRepository.save(usuario);

        analisis.setImagenesProcesadas(ocr.getImagenesProcesadas());
        analisis.setMensajesNuevos(ocr.getNuevos());
        analisis.setMensajesDuplicados(ocr.getDuplicados());
        analisis.setTotalMensajesDataset(ocr.getTotalMensajes());
        analisis.setErroresOcr(ocr.getErrores() == null
                ? Collections.emptyList()
                : ocr.getErrores());
        analisis.setMensaje(String.format(
                "Se procesaron %d capturas: %d mensajes nuevos y %d duplicados ignorados. "
                        + "El chat ya está listo para imitar a %s.",
                ocr.getImagenesProcesadas(), ocr.getNuevos(),
                ocr.getDuplicados(), nombreEx));

        log.info("Capturas procesadas para {}: {} imágenes, {} mensajes nuevos, {} duplicados",
                email, ocr.getImagenesProcesadas(), ocr.getNuevos(), ocr.getDuplicados());

        return analisis;
    }

    private String resolverNombreEx(String nombreRecibido, Usuario usuario) {
        if (nombreRecibido != null && !nombreRecibido.isBlank()) {
            return nombreRecibido.trim();
        }
        if (usuario.getNombreEx() != null && !usuario.getNombreEx().isBlank()) {
            return usuario.getNombreEx();
        }
        return "Ex";
    }

    private ExDatasetAnalisisDto analizar(String rawChat, String nombreEx, String nombreUsuario) {
        String[] lineas = rawChat == null ? new String[0] : rawChat.split("\\r?\\n");
        List<String> mensajesEx = new ArrayList<>();
        List<String> mensajesUsuario = new ArrayList<>();
        Map<String, Integer> conteoPalabras = new HashMap<>();
        Map<String, Integer> conteoEmojis = new HashMap<>();
        boolean formatoReconocido = false;

        for (String linea : lineas) {
            String clean = linea.trim();
            if (clean.isBlank()) continue;

            // Extraer emojis de todas las líneas
            Matcher emojiMatcher = EMOJI_PATTERN.matcher(clean);
            while (emojiMatcher.find()) {
                String emoji = emojiMatcher.group();
                conteoEmojis.put(emoji, conteoEmojis.getOrDefault(emoji, 0) + 1);
            }

            // Formato [fecha, hora] remitente: texto
            Matcher lineaMatcher = LINEA_CHAT_PATTERN.matcher(clean);
            if (lineaMatcher.matches()) {
                String remitente = lineaMatcher.group(1).trim();
                String texto = lineaMatcher.group(2).trim();
                if (texto.isBlank()) continue;

                formatoReconocido = true;
                if (esRemitenteUsuario(remitente, nombreUsuario)) {
                    mensajesUsuario.add(texto);
                } else {
                    mensajesEx.add(texto);
                    contarPalabras(texto, conteoPalabras);
                }
                continue;
            }

            // Compatibilidad con chats pegados sin la cabecera [fecha, hora]
            if (nombreEx != null
                    && clean.toLowerCase().contains(nombreEx.toLowerCase() + ":")) {
                int index = clean.toLowerCase().indexOf(nombreEx.toLowerCase() + ":");
                String texto = clean.substring(index + nombreEx.length() + 1).trim();
                mensajesEx.add(texto);
                contarPalabras(texto, conteoPalabras);
                formatoReconocido = true;
            } else {
                mensajesUsuario.add(clean);
            }
        }

        // Si el texto no tenía remitentes reconocibles, repartimos por líneas
        // alternas (formato antiguo de pegado libre).
        if (!formatoReconocido && mensajesEx.isEmpty()) {
            for (int i = 0; i < lineas.length; i++) {
                String clean = lineas[i].trim();
                if (clean.isBlank()) continue;
                if (i % 2 == 1) {
                    mensajesEx.add(clean);
                    contarPalabras(clean, conteoPalabras);
                } else {
                    mensajesUsuario.add(clean);
                }
            }
        }

        double longitudPromedio = mensajesEx.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        List<String> frasesFrecuentes = conteoPalabras.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(8)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> emojisFrecuentes = conteoEmojis.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        String tonoDetectado = deducirTono(
                longitudPromedio, frasesFrecuentes, rawChat == null ? "" : rawChat.toLowerCase());

        return ExDatasetAnalisisDto.builder()
                .totalLineasProcesadas(lineas.length)
                .totalMensajesEx(mensajesEx.size())
                .totalMensajesUsuario(mensajesUsuario.size())
                .longitudPromedioMensajeEx(Math.round(longitudPromedio * 100.0) / 100.0)
                .frasesFrecuentesEx(frasesFrecuentes)
                .emojisFrecuentes(emojisFrecuentes)
                .tonoDetectado(tonoDetectado)
                .build();
    }

    private boolean esRemitenteUsuario(String remitente, String nombreUsuario) {
        if (remitente == null) return false;
        String normalizado = remitente.trim();
        return normalizado.equalsIgnoreCase("tú")
                || normalizado.equalsIgnoreCase("tu")
                || normalizado.equalsIgnoreCase("yo")
                || (nombreUsuario != null
                    && normalizado.equalsIgnoreCase(nombreUsuario.trim()));
    }

    private void contarPalabras(String texto, Map<String, Integer> map) {
        String[] palabras = texto.toLowerCase()
                .replaceAll("[^a-záéíóúñ0-9 ]", "")
                .split("\\s+");
        for (String p : palabras) {
            if (p.length() > 3 && !STOP_WORDS.contains(p)) {
                map.put(p, map.getOrDefault(p, 0) + 1);
            }
        }
    }

    private String deducirTono(double longPromedio, List<String> palabras, String fullText) {
        if (longPromedio < 15.0) {
            return "FRIA_Y_CORTANTE";
        }
        if (fullText.contains("culpa") || fullText.contains("siempre")
                || fullText.contains("nunca") || fullText.contains("tú")) {
            return "TOXICA_RECLAMANTE";
        }
        if (fullText.contains("amor") || fullText.contains("te quiero")
                || fullText.contains("bebé") || fullText.contains("lindo")) {
            return "NOSTALGICA_AMBIVALENTE";
        }
        return "DISTANTE_DESINTERESADA";
    }
}

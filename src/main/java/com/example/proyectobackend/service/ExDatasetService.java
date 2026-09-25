package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.ExDatasetAnalisisDto;
import com.example.proyectobackend.dto.ExDatasetUploadDto;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Set<String> STOP_WORDS = Set.of(
            "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por",
            "un", "para", "con", "no", "una", "su", "al", "lo", "como", "mas", "pero",
            "sus", "le", "ya", "o", "este", "si", "porque", "esta", "son", "entre", "cuando"
    );

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F600}-\\x{1F64F}|\\x{1F300}-\\x{1F5FF}|\\x{1F680}-\\x{1F6FF}|\\x{1F1E0}-\\x{1F1FF}|\\x{2600}-\\x{26FF}|\\x{2700}-\\x{27BF}]",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    @Transactional
    public ExDatasetAnalisisDto procesarYEntrenarDataset(String email, ExDatasetUploadDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        String rawChat = dto.getContenidoChat();
        String nombreEx = (dto.getNombreRemitenteEx() != null && !dto.getNombreRemitenteEx().isBlank())
                ? dto.getNombreRemitenteEx().trim()
                : (usuario.getNombreEx() != null ? usuario.getNombreEx() : "Ex");

        String[] lineas = rawChat.split("\\r?\\n");
        List<String> mensajesEx = new ArrayList<>();
        List<String> mensajesUsuario = new ArrayList<>();
        Map<String, Integer> conteoPalabras = new HashMap<>();
        Map<String, Integer> conteoEmojis = new HashMap<>();

        for (String linea : lineas) {
            String clean = linea.trim();
            if (clean.isBlank()) continue;

            // Extraer emojis
            Matcher emojiMatcher = EMOJI_PATTERN.matcher(clean);
            while (emojiMatcher.find()) {
                String emoji = emojiMatcher.group();
                conteoEmojis.put(emoji, conteoEmojis.getOrDefault(emoji, 0) + 1);
            }

            // Distinguir mensajes del ex vs usuario
            if (clean.toLowerCase().contains(nombreEx.toLowerCase() + ":")) {
                int index = clean.toLowerCase().indexOf(nombreEx.toLowerCase() + ":");
                String texto = clean.substring(index + nombreEx.length() + 1).trim();
                mensajesEx.add(texto);
                contarPalabras(texto, conteoPalabras);
            } else {
                mensajesUsuario.add(clean);
            }
        }

        // Si el formato no tenía "NombreEx:", asumimos que todas las líneas pares son del ex o dividimos equitativamente
        if (mensajesEx.isEmpty()) {
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

        String tonoDetectado = deducirTono(longitudPromedio, frasesFrecuentes, rawChat.toLowerCase());

        // Guardar la personalidad y el extracto de entrenamiento en la entidad Usuario
        usuario.setPersonalidadEx(tonoDetectado);
        String resumenEntrenamiento = String.format("Frases: %s | Emojis: %s | LongitudMedia: %.1f",
                String.join(", ", frasesFrecuentes),
                String.join(" ", emojisFrecuentes),
                longitudPromedio);
        usuario.setDatasetEntrenamientoEx(resumenEntrenamiento);
        usuarioRepository.save(usuario);

        log.info("Dataset del ex procesado para el usuario {}. Tono inferido: {}", email, tonoDetectado);

        return ExDatasetAnalisisDto.builder()
                .totalLineasProcesadas(lineas.length)
                .totalMensajesEx(mensajesEx.size())
                .totalMensajesUsuario(mensajesUsuario.size())
                .longitudPromedioMensajeEx(Math.round(longitudPromedio * 100.0) / 100.0)
                .frasesFrecuentesEx(frasesFrecuentes)
                .emojisFrecuentes(emojisFrecuentes)
                .tonoDetectado(tonoDetectado)
                .mensaje("Dataset analizado y clon de personalidad actualizado exitosamente (estilo JustZando).")
                .build();
    }

    private void contarPalabras(String texto, Map<String, Integer> map) {
        String[] palabras = texto.toLowerCase().replaceAll("[^a-záéíóúñ0-9 ]", "").split("\\s+");
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
        if (fullText.contains("culpa") || fullText.contains("siempre") || fullText.contains("nunca") || fullText.contains("tú")) {
            return "TOXICA_RECLAMANTE";
        }
        if (fullText.contains("amor") || fullText.contains("te quiero") || fullText.contains("bebé") || fullText.contains("lindo")) {
            return "NOSTALGICA_AMBIVALENTE";
        }
        return "DISTANTE_DESINTERESADA";
    }
}

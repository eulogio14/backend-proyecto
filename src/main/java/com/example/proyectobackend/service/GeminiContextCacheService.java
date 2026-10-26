package com.example.proyectobackend.service;

import com.example.proyectobackend.model.ExGeminiCache;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.ExGeminiCacheRepository;
import com.example.proyectobackend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class GeminiContextCacheService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ExGeminiCacheRepository cacheRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${ia.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${ia.gemini-base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiBaseUrl;

    @Value("${ia.gemini-model:gemini-3.5-flash}")
    private String geminiModelo;

    public GeminiContextCacheService(ExGeminiCacheRepository cacheRepository,
                                     UsuarioRepository usuarioRepository) {
        this.cacheRepository = cacheRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // La cuenta gratuita de Gemini no permite cachedContents (limit=0 tokens).
    // Guardamos hasta cuándo no reintentar para no gastar una llamada por mensaje.
    private volatile long cacheDeshabilitadoHasta = 0L;

    @Transactional
    public ExGeminiCache asegurarCache(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        if (!hayTexto(geminiApiKey)) {
            return null;
        }
        if (System.currentTimeMillis() < cacheDeshabilitadoHasta) {
            return null;
        }
        var existente = cacheRepository.findByUsuarioId(usuario.getId());
        if (existente.isPresent() && existente.get().getActivo()) {
            return existente.get();
        }
        try {
            String cacheName = crearCache(usuario);
            if (hayTexto(cacheName)) {
                ExGeminiCache cache = ExGeminiCache.builder()
                        .usuario(usuario)
                        .cacheName(cacheName)
                        .modelo(geminiModelo)
                        .displayName("ex-dataset-" + usuario.getId())
                        .creadoEn(LocalDateTime.now())
                        .expiraEn(LocalDateTime.now().plusHours(24))
                        .activo(true)
                        .build();
                return cacheRepository.save(cache);
            }
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("HTTP 429") || msg.contains("HTTP 404")) {
                // Plan gratuito sin almacenamiento de cache: no reintentar por 1 hora.
                cacheDeshabilitadoHasta = System.currentTimeMillis() + 3_600_000L;
                log.info("Context caching no disponible con esta cuenta de Gemini ({}). Se usará el prompt completo.", msg);
            } else {
                log.warn("No se pudo crear contexto cache para usuario {}: {}", usuario.getId(), msg);
            }
        }
        return existente.orElse(null);
    }

    private String crearCache(Usuario usuario) throws Exception {
        String modelo = hayTexto(geminiModelo) ? geminiModelo.trim() : "gemini-3.5-flash";
        String base = hayTexto(geminiBaseUrl) ? geminiBaseUrl.trim() : "https://generativelanguage.googleapis.com/v1beta/models";
        String clave = geminiApiKey.trim();
        // El endpoint es /v1beta/cachedContents (sin /models).
        String raiz = base.replaceAll("/+$", "").replaceAll("/models$", "");
        String url = raiz + "/cachedContents?key=" + URLEncoder.encode(clave, StandardCharsets.UTF_8);

        ObjectNode payload = JSON.createObjectNode();
        payload.put("model", "models/" + modelo);
        payload.put("displayName", "ex-cache-" + usuario.getId());
        payload.put("ttl", "24h");

        ObjectNode systemInstruction = payload.putObject("systemInstruction");
        systemInstruction.putArray("parts").addObject().put("text", construirSystemPrompt(usuario));

        if (hayTexto(usuario.getDatasetEntrenamientoEx())) {
            ArrayNode contents = payload.putArray("contents");
            ObjectNode contenido = contents.addObject();
            contenido.put("role", "user");
            contenido.putArray("parts").addObject().put("text", "EJEMPLOS_REALES:\n" + usuario.getDatasetEntrenamientoEx());
            ObjectNode respuestaModelo = contents.addObject();
            respuestaModelo.put("role", "model");
            respuestaModelo.putArray("parts").addObject().put("text", "Entendido. Imitaré este estilo.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IllegalStateException("cachedContents respondió HTTP " + response.statusCode());
        }
        JsonNode root = JSON.readTree(response.body());
        return root.path("name").asText("");
    }

    private String construirSystemPrompt(Usuario usuario) {
        String nombreEx = hayTexto(usuario.getNombreEx()) ? usuario.getNombreEx().trim() : "Ex";
        String nombreUsuario = hayTexto(usuario.getNombre()) ? usuario.getNombre().trim() : "la otra persona";
        String personalidad = hayTexto(usuario.getPersonalidadEx()) ? usuario.getPersonalidadEx().trim() : "DISTANTE";
        return String.format(
                "Eres %s, una persona real que está chateando con su expareja %s.\n" +
                "Tu objetivo es responder exactamente con el estilo de escritura de %s.\n" +
                "Personalidad detectada: %s.\n\n" +
                "Reglas:\n" +
                "- Responde en español, en primera persona, como %s.\n" +
                "- Usa mensajes cortos y naturales (1 a 2 frases), con sus muletillas, faltas de ortografía y emojis.\n" +
                "- No digas que eres una IA, no expliques que estás imitando a nadie.\n" +
                "- No agregues prefijos como \"%s:\" ni comillas alrededor de la respuesta.\n",
                nombreEx, nombreUsuario, nombreEx, personalidad, nombreEx, nombreEx);
    }

    private boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}

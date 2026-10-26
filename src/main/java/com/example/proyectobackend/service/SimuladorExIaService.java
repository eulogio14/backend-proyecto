package com.example.proyectobackend.service;

import com.example.proyectobackend.model.Mensaje;
import com.example.proyectobackend.model.ModoApp;
import com.example.proyectobackend.model.Remitente;
import com.example.proyectobackend.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Genera las respuestas del Ex.
 *
 * Si hay una API de IA configurada (Gemini o cualquier API compatible con
 * OpenAI, como Groq u OpenAI), usa el dataset real del usuario como ejemplos
 * de estilo. Si no hay clave o la API falla, cae en la heurística local para
 * que el chat nunca se quede sin responder.
 */
@Slf4j
@Service
public class SimuladorExIaService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Random RANDOM = new Random();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // Sesiones de chat por usuario para mantener el contexto con Gemini
    // (Evita enviar todo el historial + dataset en cada mensaje)
    private static final class GeminiChatSession {
        String chatId;
        long ultimaActividadMs = System.currentTimeMillis();
    }

    private final Map<Long, GeminiChatSession> sesionesGemini = new ConcurrentHashMap<>();
    private final GeminiContextCacheService geminiContextCacheService;

    public SimuladorExIaService(GeminiContextCacheService geminiContextCacheService) {
        this.geminiContextCacheService = geminiContextCacheService;
    }

    // --- Configuración de la IA (variables de entorno) -------------------
    @Value("${ia.provider:}")
    private String proveedorConfigurado;

    @Value("${ia.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${ia.gemini-base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiBaseUrl;

    @Value("${ia.gemini-model:gemini-2.5-flash-lite}")
    private String geminiModelo;

    @Value("${ia.openai-api-key:}")
    private String openAiApiKey;

    @Value("${ia.openai-base-url:https://api.groq.com/openai/v1}")
    private String openAiBaseUrl;

    @Value("${ia.openai-model:llama-3.3-70b-versatile}")
    private String openAiModelo;

    @Value("${ia.max-output-tokens:150}")
    private int maxTokens;

    @Value("${ia.temperature:0.8}")
    private double temperatura;

    @Value("${ia.dataset-max-chars:6000}")
    private int datasetMaxChars;

    @Value("${ia.history-max-messages:12}")
    private int historyMaxMessages;

    @PostConstruct
    void registrarConfiguracionIa() {
        String proveedor = resolverProveedor();
        if (proveedor == null) {
            log.warn("No hay API key de IA configurada; el chat usará la heurística local.");
        } else {
            String modelo = "gemini".equals(proveedor) ? geminiModelo : openAiModelo;
            log.info("IA configurada: {} (modelo {}).", proveedor, modelo);
        }
    }

    public String generarRespuesta(Usuario usuario, String mensajeUsuario, ModoApp modo) {
        return generarRespuesta(usuario, mensajeUsuario, modo, Collections.emptyList());
    }

    public String generarRespuesta(Usuario usuario, String mensajeUsuario, ModoApp modo,
                                   List<Mensaje> historial) {
        // Modo SUPERAR eliminado. Siempre se usa el chat con el Ex/IA.

        String proveedor = resolverProveedor();
        if (proveedor != null) {
            try {
                String respuesta = "gemini".equals(proveedor)
                        ? llamarGemini(usuario, mensajeUsuario, historial)
                        : llamarOpenAiCompatible(usuario, mensajeUsuario, historial);

                if (respuesta != null && !respuesta.isBlank()) {
                    return limpiarRespuesta(respuesta);
                }
            } catch (Exception e) {
                log.warn("La IA ({}) no respondió: {}. Se usará la heurística local.",
                        proveedor, e.getMessage());
            }
        }

        return generarRespuestaModoEx(usuario, mensajeUsuario);
    }

    // ---------------------------------------------------------------------
    // Proveedores de IA
    // ---------------------------------------------------------------------
    private String resolverProveedor() {
        String configurado = proveedorConfigurado == null
                ? "" : proveedorConfigurado.trim().toLowerCase();

        if (configurado.equals("openai") && hayTexto(openAiApiKey)) {
            return "openai";
        }
        if (configurado.equals("gemini") && hayTexto(geminiApiKey)) {
            return "gemini";
        }
        if (hayTexto(geminiApiKey)) {
            return "gemini";
        }
        if (hayTexto(openAiApiKey)) {
            return "openai";
        }
        return null;
    }

    private String llamarGemini(Usuario usuario, String mensajeUsuario,
                                List<Mensaje> historial) throws Exception {
        try {
            geminiContextCacheService.asegurarCache(usuario);
        } catch (Exception e) {
            log.debug("No se pudo asegurar cache de Gemini: {}", e.getMessage());
        }

        String modelo = hayTexto(geminiModelo) ? geminiModelo.trim() : "gemini-3.5-flash";
        String base = hayTexto(geminiBaseUrl)
                ? geminiBaseUrl.trim()
                : "https://generativelanguage.googleapis.com/v1beta/models";
        String url = base.replaceAll("/+$", "") + "/" + modelo
                + ":generateContent?key="
                + URLEncoder.encode(geminiApiKey.trim(), StandardCharsets.UTF_8);

        ObjectNode payload = JSON.createObjectNode();
        ObjectNode systemInstruction = payload.putObject("system_instruction");
        systemInstruction.putArray("parts")
                .addObject()
                .put("text", construirPromptSistema(usuario));

        ArrayNode contents = payload.putArray("contents");
        ObjectNode contenido = contents.addObject();
        contenido.put("role", "user");
        contenido.putArray("parts")
                .addObject()
                .put("text", construirPromptUsuario(usuario, mensajeUsuario, historial));

        ObjectNode generacion = payload.putObject("generationConfig");
        generacion.put("temperature", temperaturaEfectiva());
        generacion.put("maxOutputTokens", maxTokensEfectivo());
        // Desactivamos el thinking en modelos con razonamiento para evitar
        // respuestas vacías y reducir tiempo de respuesta.
        if (!modelo.toLowerCase().contains("lite")) {
            generacion.putObject("thinkingConfig").put("thinkingBudget", 0);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Gemini respondió HTTP " + response.statusCode()
                    + ": " + recortarTexto(response.body(), 300));
        }

        JsonNode root = JSON.readTree(response.body());
        JsonNode partes = root.path("candidates").path(0).path("content").path("parts");
        StringBuilder texto = new StringBuilder();
        for (JsonNode parte : partes) {
            // Ignoramos los fragmentos internos de razonamiento.
            if (!parte.path("thought").asBoolean(false)) {
                texto.append(parte.path("text").asText(""));
            }
        }
        return texto.toString();
    }

    private String llamarOpenAiCompatible(Usuario usuario, String mensajeUsuario,
                                         List<Mensaje> historial) throws Exception {
        String base = hayTexto(openAiBaseUrl)
                ? openAiBaseUrl.trim()
                : "https://api.groq.com/openai/v1";
        String url = base.replaceAll("/+$", "") + "/chat/completions";
        String modelo = hayTexto(openAiModelo)
                ? openAiModelo.trim()
                : "llama-3.3-70b-versatile";

        ObjectNode payload = JSON.createObjectNode();
        payload.put("model", modelo);
        payload.put("temperature", temperaturaEfectiva());
        payload.put("max_tokens", maxTokensEfectivo());

        ArrayNode messages = payload.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", construirPromptSistema(usuario));
        messages.addObject()
                .put("role", "user")
                .put("content", construirPromptUsuario(usuario, mensajeUsuario, historial));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("La API de IA respondió HTTP "
                    + response.statusCode());
        }

        JsonNode root = JSON.readTree(response.body());
        return root.path("choices").path(0).path("message").path("content").asText("");
    }

    private String construirPromptSistema(Usuario usuario) {
        String nombreEx = hayTexto(usuario.getNombreEx()) ? usuario.getNombreEx().trim() : "Ex";
        String nombreUsuario = hayTexto(usuario.getNombre()) ? usuario.getNombre().trim() : "la otra persona";
        String personalidad = hayTexto(usuario.getPersonalidadEx())
                ? usuario.getPersonalidadEx().trim()
                : "DISTANTE";

        return """
                Eres %s, una persona real que está chateando con su expareja %s.
                Tu objetivo es responder exactamente con el estilo de escritura de %s.

                --- EJEMPLOS REALES EXTRAÍDOS DE SU CHAT ---
                %s
                --- FIN DE LOS EJEMPLOS ---

                Personalidad detectada: %s.

                Reglas:
                - Responde en español, en primera persona, como %s.
                - Usa mensajes cortos y naturales (1 a 2 frases), con sus muletillas, faltas de ortografía y emojis.
                - No digas que eres una IA, no expliques que estás imitando a nadie.
                - No agregues prefijos como "%s:" ni comillas alrededor de la respuesta.
                """.formatted(
                nombreEx, nombreUsuario, nombreEx,
                recortarDataset(usuario.getDatasetEntrenamientoEx()),
                personalidad, nombreEx, nombreEx);
    }

    private String construirPromptUsuario(Usuario usuario, String mensajeUsuario,
                                          List<Mensaje> historial) {
        String nombreEx = hayTexto(usuario.getNombreEx()) ? usuario.getNombreEx().trim() : "Ex";
        return """
                Conversación reciente:
                %s

                Mensaje nuevo de %s: "%s"

                Responde como %s (solo el texto del mensaje):
                """.formatted(
                formatearHistorial(historial, usuario),
                hayTexto(usuario.getNombre()) ? usuario.getNombre() : "el usuario",
                mensajeUsuario == null ? "" : mensajeUsuario,
                nombreEx);
    }

    private String construirPromptUsuarioConHistoria(Usuario usuario, String mensajeUsuario,
                                                    List<Mensaje> historial) {
        String nombreEx = hayTexto(usuario.getNombreEx()) ? usuario.getNombreEx().trim() : "Ex";
        String hist = formatearHistorial(historial, usuario);
        if (!hayTexto(hist) || "(sin mensajes anteriores)".equals(hist)) {
            return String.format("Mensaje de %s: \"%s\"\nResponde como %s (solo el texto):",
                    hayTexto(usuario.getNombre()) ? usuario.getNombre() : "el usuario",
                    mensajeUsuario == null ? "" : mensajeUsuario,
                    nombreEx);
        }
        return String.format("Conversación reciente:\n%s\n\nMensaje nuevo de %s: \"%s\"\nResponde como %s (solo el texto):",
                hist,
                hayTexto(usuario.getNombre()) ? usuario.getNombre() : "el usuario",
                mensajeUsuario == null ? "" : mensajeUsuario,
                nombreEx);
    }

    private String recortarDataset(String dataset) {
        if (!hayTexto(dataset)) {
            return "(No hay ejemplos guardados; usa la personalidad indicada.)";
        }
        int limite = datasetMaxChars > 0 ? datasetMaxChars : 6000;
        String limpio = dataset.trim();
        if (limpio.length() <= limite) {
            return limpio;
        }
        // Conservamos la parte más reciente del chat.
        return "...\n" + limpio.substring(limpio.length() - limite);
    }

    private String formatearHistorial(List<Mensaje> historial, Usuario usuario) {
        if (historial == null || historial.isEmpty()) {
            return "(sin mensajes anteriores)";
        }

        int limite = historyMaxMessages > 0 ? historyMaxMessages : 12;
        int inicio = Math.max(0, historial.size() - limite);
        List<Mensaje> recientes = new ArrayList<>(historial.subList(inicio, historial.size()));

        String nombreEx = hayTexto(usuario.getNombreEx()) ? usuario.getNombreEx().trim() : "Ex";
        StringBuilder texto = new StringBuilder();
        for (Mensaje mensaje : recientes) {
            String autor = mensaje.getRemitente() == Remitente.USUARIO
                    ? "Usuario" : nombreEx;
            texto.append(autor)
                    .append(": ")
                    .append(recortarTexto(mensaje.getContenido(), 300))
                    .append('\n');
        }
        return texto.toString().trim();
    }

    private String recortarTexto(String texto, int maximo) {
        if (texto == null) return "";
        String limpio = texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo) + "...";
    }

    private String limpiarRespuesta(String respuesta) {
        String limpio = respuesta.trim();
        limpio = limpio.replaceAll("^```(?:\\w+)?\\s*", "")
                .replaceAll("\\s*```$", "");
        if (limpio.length() >= 2 && limpio.startsWith("\"") && limpio.endsWith("\"")) {
            limpio = limpio.substring(1, limpio.length() - 1);
        }
        limpio = limpio.replaceAll("(?i)^(ex|yo|respuesta)\\s*:\\s*", "");
        if (limpio.length() > 500) {
            limpio = limpio.substring(0, 500).trim();
        }
        return limpio;
    }

    private double temperaturaEfectiva() {
        return temperatura > 0 ? temperatura : 0.8;
    }

    private int maxTokensEfectivo() {
        return maxTokens > 0 ? maxTokens : 150;
    }

    private boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    // ---------------------------------------------------------------------
    // Heurística local (fallback sin API)
    // ---------------------------------------------------------------------
    private String generarRespuestaModoEx(Usuario usuario, String mensaje) {
        String msgLower = mensaje == null ? "" : mensaje.toLowerCase();
        String personalidad = usuario.getPersonalidadEx() != null
                ? usuario.getPersonalidadEx() : "DISTANTE";

        if (msgLower.contains("hola") || msgLower.contains("buenas")) {
            if (personalidad.contains("FRIA")) {
                return randomChoice(List.of(
                        "Hola. ¿Qué quieres?",
                        "¿Qué pasó? Dime rápido.",
                        "Hola. Ocupada."));
            }
            if (personalidad.contains("TOXICA")) {
                return randomChoice(List.of(
                        "Ah, ¿ahora sí te acuerdas de que existo?",
                        "¿Por qué me escribes a esta hora? Siempre lo mismo contigo."));
            }
            return randomChoice(List.of(
                    "Hola... ¿qué pasó? Pensé que habíamos quedado en darnos espacio.",
                    "Hola. ¿Todo bien? No esperaba un mensaje tuyo.",
                    "Hola. Justo estaba ocupada.",
                    "¿Qué pasó? Dime rápido porfa."));
        }

        if (msgLower.contains("te extraño") || msgLower.contains("volver")) {
            if (personalidad.contains("FRIA")) {
                return randomChoice(List.of(
                        "Ya supérame, por favor.",
                        "No siento lo mismo.",
                        "Es tu problema, no el mío."));
            }
            return randomChoice(List.of(
                    "No hagas esto más difícil, por favor. Ya hablamos de esto.",
                    "Yo también pienso en cómo eran las cosas antes... pero sabes que nos hacíamos daño.",
                    "Por favor, no me escribas estas cosas ahora. Necesitamos avanzar.",
                    "Ya es tarde para eso. Tuviste tu oportunidad."));
        }

        if (msgLower.contains("donde estas") || msgLower.contains("con quien")) {
            return randomChoice(List.of(
                    "Eso ya no es asunto tuyo, la verdad.",
                    "Saliendo con unos amigos. Por favor cuídate.",
                    "¿Por qué me preguntas eso? Ya no somos nada."));
        }

        if (msgLower.contains("perdon") || msgLower.contains("disculpa")
                || msgLower.contains("lo siento")) {
            return randomChoice(List.of(
                    "Agradezco que lo digas, pero las palabras ya no cambian lo que pasó.",
                    "Está bien, te perdono. Pero eso no significa que podamos volver a como era antes.",
                    "Ya pasó mucho tiempo. Ojalá los dos encontremos paz."));
        }

        if (personalidad.contains("FRIA")) {
            return randomChoice(List.of("Ok.", "👍", "Leído.",
                    "No tengo nada que decir.", "De verdad no me escribas."));
        }

        return randomChoice(List.of(
                "Leído. No sé qué decirte a eso.",
                "Ojalá te vaya bien en tus cosas, de verdad.",
                "Por favor cuídate mucho. Ya no me busques.",
                "Me tengo que ir, hablamos luego (o mejor no).",
                "Ok.",
                "👍"));
    }



    private String randomChoice(List<String> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}

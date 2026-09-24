package com.example.proyectobackend.service;

import com.example.proyectobackend.model.ModoApp;
import com.example.proyectobackend.model.Usuario;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class SimuladorExIaService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(800))
            .build();

    // URL donde corre el microservicio de PyTorch o Transformer inspirado en JustZando
    private final String PYTHON_AI_URL = "http://localhost:8000/predict";

    public String generarRespuesta(Usuario usuario, String mensajeUsuario, ModoApp modo) {
        // 1. Intentar llamar al modelo de PyTorch/FastAPI si está activo
        try {
            String jsonPayload = String.format(
                    "{\"mensaje\":\"%s\", \"modo\":\"%s\", \"personalidad\":\"%s\", \"datasetContext\":\"%s\"}",
                    escapeJson(mensajeUsuario),
                    modo.name(),
                    usuario.getPersonalidadEx(),
                    escapeJson(usuario.getDatasetEntrenamientoEx() != null ? usuario.getDatasetEntrenamientoEx() : "")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PYTHON_AI_URL))
                    .timeout(Duration.ofSeconds(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                return response.body();
            }
        } catch (Exception ignored) {
            // El microservicio de Python no está activo, aplicamos la heurística simulada condicionada
        }

        // 2. Heurística de simulación según el MODO y la PERSONALIDAD aprendida
        if (modo == ModoApp.SUPERAR) {
            return generarRespuestaModoSuperar(mensajeUsuario);
        } else {
            return generarRespuestaModoEx(usuario, mensajeUsuario);
        }
    }

    private String generarRespuestaModoEx(Usuario usuario, String mensaje) {
        String msgLower = mensaje.toLowerCase();
        String personalidad = usuario.getPersonalidadEx() != null ? usuario.getPersonalidadEx() : "DISTANTE";

        if (msgLower.contains("hola") || msgLower.contains("buenas")) {
            if (personalidad.contains("FRIA")) {
                return randomChoice(List.of("Hola. ¿Qué quieres?", "¿Qué pasó? Dime rápido.", "Hola. Ocupada."));
            }
            if (personalidad.contains("TOXICA")) {
                return randomChoice(List.of("Ah, ¿ahora sí te acuerdas de que existo?", "¿Por qué me escribes a esta hora? Siempre lo mismo contigo."));
            }
            List<String> respuestas = List.of(
                    "Hola... ¿qué pasó? Pensé que habíamos quedado en darnos espacio.",
                    "Hola. ¿Todo bien? No esperaba un mensaje tuyo.",
                    "Hola. Justo estaba ocupada.",
                    "¿Qué pasó? Dime rápido porfa."
            );
            return randomChoice(respuestas);
        }

        if (msgLower.contains("te extraño") || msgLower.contains("te extraño mucho") || msgLower.contains("volver")) {
            if (personalidad.contains("FRIA")) {
                return randomChoice(List.of("Ya supérame, por favor.", "No siento lo mismo.", "Es tu problema, no el mío."));
            }
            List<String> respuestas = List.of(
                    "No hagas esto más difícil, por favor. Ya hablamos de esto.",
                    "Yo también pienso en cómo eran las cosas antes... pero sabes que nos hacíamos daño.",
                    "Por favor, no me escribas estas cosas ahora. Necesitamos avanzar.",
                    "Ya es tarde para eso. Tuviste tu oportunidad."
            );
            return randomChoice(respuestas);
        }

        if (msgLower.contains("donde estas") || msgLower.contains("con quien")) {
            List<String> respuestas = List.of(
                    "Eso ya no es asunto tuyo, la verdad.",
                    "Saliendo con unos amigos. Por favor cuídate.",
                    "¿Por qué me preguntas eso? Ya no somos nada."
            );
            return randomChoice(respuestas);
        }

        if (msgLower.contains("perdon") || msgLower.contains("disculpa") || msgLower.contains("lo siento")) {
            List<String> respuestas = List.of(
                    "Agradezco que lo digas, pero las palabras ya no cambian lo que pasó.",
                    "Está bien, te perdono. Pero eso no significa que podamos volver a como era antes.",
                    "Ya pasó mucho tiempo. Ojalá los dos encontremos paz."
            );
            return randomChoice(respuestas);
        }

        // Respuestas genéricas condicionadas
        if (personalidad.contains("FRIA")) {
            return randomChoice(List.of("Ok.", "👍", "Leído.", "No tengo nada que decir.", "De verdad no me escribas."));
        }

        List<String> genericas = List.of(
                "Leído. No sé qué decirte a eso.",
                "Ojalá te vaya bien en tus cosas, de verdad.",
                "Por favor cuídate mucho. Ya no me busques.",
                "Me tengo que ir, hablamos luego (o mejor no).",
                "Ok.",
                "👍"
        );
        return randomChoice(genericas);
    }

    private String generarRespuestaModoSuperar(String mensaje) {
        List<String> consejos = List.of(
                "🛡️ [Asistente de Superación]: Respira profundo. Recuerda por qué decidiste empezar el contacto cero.",
                "💪 [Fuerza Mental]: Las ganas de escribirle solo duran 15 minutos en su pico más alto. Toma un vaso de agua y aléjate del teléfono.",
                "🧠 [Realidad]: No extrañas a esa persona, extrañas la dopamina y la ilusión de lo que esperabas que fuera. Revisa tu lista de Red Flags.",
                "✨ [Progreso]: Cada hora que pasas sin buscar su perfil es un día ganado para tu paz futura. ¡Tú tienes el control!"
        );
        return randomChoice(consejos);
    }

    private String randomChoice(List<String> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private String escapeJson(String raw) {
        return raw.replace("\"", "\\\"").replace("\n", "\\n");
    }
}

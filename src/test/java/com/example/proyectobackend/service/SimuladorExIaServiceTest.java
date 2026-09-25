package com.example.proyectobackend.service;

import com.example.proyectobackend.model.ModoApp;
import com.example.proyectobackend.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorExIaServiceTest {

    private SimuladorExIaService simuladorService;
    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        simuladorService = new SimuladorExIaService();
        usuarioPrueba = Usuario.builder()
                .id(1L)
                .nombre("Rick")
                .email("rick@dimensionc137.com")
                .nombreEx("Diane")
                .personalidadEx("Fría y distante")
                .modoActual(ModoApp.RECORDAR)
                .build();
    }

    @Test
    @DisplayName("Modo SUPERAR debe retornar mensajes de contención psicológica")
    void testGenerarRespuestaModoSuperar() {
        String respuesta = simuladorService.generarRespuesta(usuarioPrueba, "Quiero escribirle ya no aguanto", ModoApp.SUPERAR);

        assertNotNull(respuesta);
        assertFalse(respuesta.isBlank());
        assertTrue(
                respuesta.contains("Asistente de Superación") ||
                respuesta.contains("Fuerza Mental") ||
                respuesta.contains("Realidad") ||
                respuesta.contains("Progreso"),
                "La respuesta debe pertenecer al catálogo de contención en Modo SUPERAR"
        );
    }

    @Test
    @DisplayName("Modo EX con mensaje de extrañar debe simular rechazo o distancia")
    void testGenerarRespuestaModoExTeExtraño() {
        String respuesta = simuladorService.generarRespuesta(usuarioPrueba, "Te extraño mucho, por favor vuelve", ModoApp.RECORDAR);

        assertNotNull(respuesta);
        assertFalse(respuesta.isBlank());
        assertTrue(
                respuesta.contains("difícil") ||
                respuesta.contains("daño") ||
                respuesta.contains("avanzar") ||
                respuesta.contains("tarde") ||
                respuesta.contains("Leído") ||
                respuesta.contains("cuídate") ||
                respuesta.contains("Ok") ||
                respuesta.contains("👍"),
                "La simulación del ex debe responder con distanciamiento realista"
        );
    }
}

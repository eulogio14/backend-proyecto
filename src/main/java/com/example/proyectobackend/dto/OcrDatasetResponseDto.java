package com.example.proyectobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta del microservicio Python que convierte capturas de WhatsApp
 * en el dataset de texto del Ex.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrDatasetResponseDto {

    private String usuarioId;
    private String texto;
    private int nuevos;
    private int duplicados;
    private int imagenesProcesadas;
    private int totalMensajes;
    private List<String> errores = new ArrayList<>();
}

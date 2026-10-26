package com.example.proyectobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExDatasetAnalisisDto {
    private int totalLineasProcesadas;
    private int totalMensajesEx;
    private int totalMensajesUsuario;
    private double longitudPromedioMensajeEx;
    private List<String> frasesFrecuentesEx;
    private List<String> emojisFrecuentes;
    private String tonoDetectado;
    private String mensaje;

    // Datos extra cuando el dataset se generó desde capturas de WhatsApp
    private int imagenesProcesadas;
    private int mensajesNuevos;
    private int mensajesDuplicados;
    private int totalMensajesDataset;
    private List<String> erroresOcr;
}

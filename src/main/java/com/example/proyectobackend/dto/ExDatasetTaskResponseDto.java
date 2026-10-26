package com.example.proyectobackend.dto;

import com.example.proyectobackend.model.ExDatasetTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExDatasetTaskResponseDto {
    private Long taskId;
    private ExDatasetTaskStatus estado;
    private String mensaje;
    private Integer imagenesProcesadas;
    private Integer mensajesNuevos;
    private Integer mensajesDuplicados;
    private Integer totalMensajesDataset;
    private Integer totalMensajesEx;
    private String tonoDetectado;
    private LocalDateTime creadoEn;
    private LocalDateTime iniciadoEn;
    private LocalDateTime terminadoEn;
    private String errorMensaje;
}

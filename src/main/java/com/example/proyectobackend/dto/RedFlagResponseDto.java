package com.example.proyectobackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedFlagResponseDto {

    @Schema(description = "Identificador único de la alerta", example = "1")
    private Long id;

    @Schema(description = "Título de la red flag", example = "Gaslighting y manipulación")
    private String titulo;

    @Schema(description = "Descripción del comportamiento", example = "Minimizaba mis sentimientos diciendo que exageraba")
    private String descripcion;

    @Schema(description = "Nivel de gravedad (1 a 5)", example = "4")
    private int gravedad;

    @Schema(description = "Fecha de registro", example = "2026-09-25T11:00:00")
    private LocalDateTime fechaCreacion;
}

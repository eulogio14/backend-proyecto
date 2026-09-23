package com.example.proyectobackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecuerdoResponseDto {

    @Schema(description = "Identificador único del recuerdo", example = "1")
    private Long id;

    @Schema(description = "Título del recuerdo", example = "Viaje a la playa")
    private String titulo;

    @Schema(description = "Descripción del recuerdo", example = "El día que fuimos al sur y vimos el atardecer")
    private String descripcion;

    @Schema(description = "URL de la foto", example = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
    private String urlFoto;

    @Schema(description = "Fecha del recuerdo", example = "2025-02-14")
    private LocalDate fechaRecuerdo;

    @Schema(description = "Nivel de nostalgia (1 a 5)", example = "4")
    private int nivelNostalgia;

    @Schema(description = "Tipo de recuerdo", example = "FOTO")
    private String tipo;

    @Schema(description = "Fecha de registro en la app", example = "2026-09-25T11:00:00")
    private LocalDateTime fechaCreacion;
}

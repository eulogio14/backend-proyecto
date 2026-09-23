package com.example.proyectobackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecuerdoDto {

    @NotBlank(message = "El título del recuerdo es obligatorio")
    @Schema(description = "Título del recuerdo", example = "Viaje a la playa")
    private String titulo;

    @Schema(description = "Descripción detallada del recuerdo", example = "El día que fuimos al sur y vimos el atardecer")
    private String descripcion;

    @Schema(description = "URL de la fotografía", example = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
    private String urlFoto;

    @Schema(description = "Fecha en que ocurrió el recuerdo", example = "2025-02-14")
    private LocalDate fechaRecuerdo;

    @Min(value = 1, message = "El nivel de nostalgia mínimo es 1")
    @Max(value = 5, message = "El nivel de nostalgia máximo es 5")
    @Schema(description = "Nivel de nostalgia del 1 al 5", example = "4", defaultValue = "3")
    private int nivelNostalgia = 3;

    @Schema(description = "Tipo de recuerdo (FOTO, CARTA, CANCION, LUGAR)", example = "FOTO")
    private String tipo;
}

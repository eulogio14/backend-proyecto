package com.example.proyectobackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedFlagDto {

    @NotBlank(message = "El título del Red Flag es obligatorio")
    @Schema(description = "Título o nombre de la conducta tóxica detectada", example = "Gaslighting y manipulación")
    private String titulo;

    @Schema(description = "Detalle de la conducta ocurrida", example = "Minimizaba mis sentimientos diciendo que exageraba y que todo era mi culpa")
    private String descripcion;

    @Min(value = 1, message = "La gravedad mínima es 1")
    @Max(value = 5, message = "La gravedad máxima es 5")
    @Schema(description = "Nivel de gravedad o toxicidad en escala del 1 al 5", example = "4", defaultValue = "3")
    private int gravedad = 3;
}

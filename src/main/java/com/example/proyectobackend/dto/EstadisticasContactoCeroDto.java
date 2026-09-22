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
public class EstadisticasContactoCeroDto {

    @Schema(description = "Días consecutivos de contacto cero acumulados", example = "15")
    private int diasRachaActual;

    @Schema(description = "Racha máxima histórica sin recaídas", example = "42")
    private int rachaMaxima;

    @Schema(description = "Cantidad total de recaídas registradas", example = "2")
    private long totalRecaidas;

    @Schema(description = "Fecha de inicio del conteo actual", example = "2026-09-10T00:00:00")
    private LocalDateTime fechaInicioContactoCero;
}

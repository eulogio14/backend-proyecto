package com.example.proyectobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasGlobalesDto {
    private long totalUsuarios;
    private long totalRecaidasRegistradas;
    private long totalEntradasDiario;
    private long totalRedFlags;
    private long totalMensajesIntercambiados;
}

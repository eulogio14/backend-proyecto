package com.example.proyectobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiarioResponseDto {
    private Long id;
    private String titulo;
    private String contenido;
    private String estadoAnimo;
    private LocalDateTime fechaCreacion;
}

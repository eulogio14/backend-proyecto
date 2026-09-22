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
public class RecaidaResponseDto {
    private Long id;
    private String motivo;
    private LocalDateTime fechaRecaida;
}

package com.example.proyectobackend.dto;

import com.example.proyectobackend.model.EstadoMensaje;
import com.example.proyectobackend.model.Remitente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDto {
    private Long id;
    private String contenido;
    private Remitente remitente;
    private EstadoMensaje estado;
    private LocalDateTime fechaEnvio;
}

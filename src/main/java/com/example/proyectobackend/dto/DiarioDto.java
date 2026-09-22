package com.example.proyectobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiarioDto {
    @NotBlank(message = "El título de la entrada es obligatorio")
    private String titulo;

    @NotBlank(message = "El contenido del diario es obligatorio")
    private String contenido;

    private String estadoAnimo;
}

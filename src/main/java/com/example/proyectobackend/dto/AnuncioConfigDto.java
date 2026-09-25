package com.example.proyectobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioConfigDto {
    private Long id;

    @NotBlank(message = "La clave del anuncio es obligatoria")
    private String clave;

    private String titulo;
    private String adUnitId;
    private boolean activo;
    private int frecuenciaMensajes;
    private String descripcion;
}

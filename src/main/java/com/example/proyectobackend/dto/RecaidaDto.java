package com.example.proyectobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecaidaDto {
    @NotBlank(message = "El motivo de la recaída es obligatorio")
    private String motivo;
}

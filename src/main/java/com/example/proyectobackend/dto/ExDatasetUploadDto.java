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
public class ExDatasetUploadDto {

    @NotBlank(message = "El contenido del chat es obligatorio para el análisis")
    private String contenidoChat;

    private String nombreRemitenteEx; // Nombre que usa el ex en el chat exportado (ej: "Camila")
}

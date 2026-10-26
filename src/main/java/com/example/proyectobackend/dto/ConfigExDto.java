package com.example.proyectobackend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConfigExDto {
    private String nombreEx;
    private String fotoPerfilEx;
    private String estadoWhatsAppEx;
    private String personalidadEx;
    private LocalDate fechaRuptura;
}

package com.example.proyectobackend.dto;

import com.example.proyectobackend.model.ModoApp;
import com.example.proyectobackend.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDto {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private ModoApp modoActual;
    private String nombreEx;
    private String apodoEx;
    private String fotoPerfilEx;
    private String estadoWhatsAppEx;
    private String personalidadEx;
    private LocalDate fechaRuptura;
    private LocalDateTime fechaInicioContactoCero;
    private int diasRachaContactoCero;
    private int rachaMaximaContactoCero;
    private LocalDateTime fechaRegistro;
}

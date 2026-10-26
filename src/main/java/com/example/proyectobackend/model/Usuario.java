package com.example.proyectobackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Rol rol = Rol.ROLE_USER;

    // --- Perfil del Ex para la apariencia de WhatsApp ---
    @Column(nullable = false)
    private String nombreEx = "Mi Ex";

    @Column(name = "foto_perfil_ex", columnDefinition = "TEXT")
    private String fotoPerfilEx;

    @Column(name = "foto_perfil_user", columnDefinition = "TEXT")
    private String fotoPerfil;


    @Builder.Default
    private String personalidadEx = "NOSTALGICA_DISTANTE"; // FRIA, CARIÑOSA, TOXICA, NOSTALGICA_DISTANTE

    @Column(columnDefinition = "TEXT")
    private String datasetEntrenamientoEx;

    // --- Metricas de Ruptura y Contacto Cero ---
    private LocalDate fechaRuptura;

    private LocalDateTime fechaInicioContactoCero;


    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}

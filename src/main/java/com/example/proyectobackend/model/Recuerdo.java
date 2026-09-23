package com.example.proyectobackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "recuerdos")
public class Recuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Usuario usuario;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 3000)
    private String descripcion;

    private String urlFoto;

    private LocalDate fechaRecuerdo;

    @Builder.Default
    private int nivelNostalgia = 5; // Escala 1 al 10

    @Builder.Default
    private String tipo = "FOTO"; // FOTO, CARTA, CANCION, LUGAR

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}

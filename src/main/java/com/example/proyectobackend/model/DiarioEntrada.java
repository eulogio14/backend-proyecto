package com.example.proyectobackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diario_entradas")
public class DiarioEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Usuario usuario;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 4000, nullable = false)
    private String contenido;

    @Builder.Default
    private String estadoAnimo = "EN_PROCESO"; // TRISTE, ENOJADO, EN_PAZ, MOTIVADO, RECAIDA

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}

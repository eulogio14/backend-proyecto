package com.example.proyectobackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anuncios_config")
public class AnuncioConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clave; // Ej: "BANNER_CHAT", "INTERSTITIAL_MODO", "REWARDED_RECUERDO"

    private String titulo;

    private String adUnitId; // ID de AdMob o red publicitaria

    @Builder.Default
    private boolean activo = true;

    @Builder.Default
    private int frecuenciaMensajes = 5; // Mostrar cada N mensajes

    private String descripcion;
}

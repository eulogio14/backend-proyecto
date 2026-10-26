package com.example.proyectobackend.model;

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
@Table(name = "ex_gemini_cache")
public class ExGeminiCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "cache_name", nullable = false)
    private String cacheName;

    @Column(columnDefinition = "TEXT")
    private String modelo;

    @Column(columnDefinition = "TEXT")
    private String displayName;

    private LocalDateTime creadoEn;

    private LocalDateTime expiraEn;

    private Boolean activo;
}

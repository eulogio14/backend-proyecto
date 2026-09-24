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
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "usuario"})
    private Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Remitente remitente;

    @Column(length = 2500, nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoMensaje estado = EstadoMensaje.LEIDO;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

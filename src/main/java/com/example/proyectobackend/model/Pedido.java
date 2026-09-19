package com.example.proyectobackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "lavadora_id")
    private Lavadora lavadora;

    private String modoLavado; // NORMAL, RAPIDO, ULTRARAPIDO
    private String fragancia;
    private String estado; // PENDIENTE, EN_PROGRESO, FINALIZADO
    private LocalDateTime fechaReserva;
}


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
@Table(name = "ex_dataset_tasks")
public class ExDatasetTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExDatasetTaskStatus estado = ExDatasetTaskStatus.QUEUED;

    @Column(name = "nombre_remitente_ex")
    private String nombreRemitenteEx;

    @Column(columnDefinition = "TEXT")
    private String resultadoTexto;

    @Column(columnDefinition = "TEXT")
    private String errorMensaje;

    private Integer imagenesProcesadas;

    private Integer mensajesNuevos;

    private Integer mensajesDuplicados;

    private Integer totalMensajesDataset;

    private Integer totalMensajesEx;

    private String tonoDetectado;

    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    private LocalDateTime iniciadoEn;

    private LocalDateTime terminadoEn;
}

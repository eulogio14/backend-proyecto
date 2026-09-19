package com.example.proyectobackend.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "lavadoras")
public class Lavadora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo; // Ej: LAV-01
    private String estado; // LIBRE, OCUPADA, MANTENIMIENTO

}
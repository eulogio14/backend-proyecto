package com.example.proyectobackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name ="usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;


}

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
    private String email;
    private String password;


}

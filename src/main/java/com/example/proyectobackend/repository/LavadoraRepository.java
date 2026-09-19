package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.Lavadora;
import com.example.proyectobackend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LavadoraRepository extends JpaRepository<Lavadora, Long> {
}

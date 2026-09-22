package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.DiarioEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiarioEntradaRepository extends JpaRepository<DiarioEntrada, Long> {
    List<DiarioEntrada> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}

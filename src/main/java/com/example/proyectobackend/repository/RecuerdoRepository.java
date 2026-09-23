package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.Recuerdo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecuerdoRepository extends JpaRepository<Recuerdo, Long> {
    List<Recuerdo> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}

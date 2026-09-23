package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.RedFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedFlagRepository extends JpaRepository<RedFlag, Long> {
    List<RedFlag> findByUsuarioIdOrderByGravedadDesc(Long usuarioId);
}

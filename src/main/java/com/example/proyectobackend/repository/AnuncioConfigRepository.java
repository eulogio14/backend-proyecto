package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.AnuncioConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnuncioConfigRepository extends JpaRepository<AnuncioConfig, Long> {
    Optional<AnuncioConfig> findByClave(String clave);
    List<AnuncioConfig> findByActivoTrue();
}

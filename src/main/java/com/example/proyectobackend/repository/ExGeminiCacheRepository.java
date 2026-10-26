package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.ExGeminiCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExGeminiCacheRepository extends JpaRepository<ExGeminiCache, Long> {
    Optional<ExGeminiCache> findByUsuarioId(Long usuarioId);
}

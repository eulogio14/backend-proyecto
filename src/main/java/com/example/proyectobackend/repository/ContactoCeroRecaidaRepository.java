package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.ContactoCeroRecaida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactoCeroRecaidaRepository extends JpaRepository<ContactoCeroRecaida, Long> {
    List<ContactoCeroRecaida> findByUsuarioIdOrderByFechaRecaidaDesc(Long usuarioId);
    long countByUsuarioId(Long usuarioId);
}

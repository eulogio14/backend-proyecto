package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByChatIdOrderByTimestampAsc(Long chatId);
    long countByChatId(Long chatId);
}

package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.Chat;
import com.example.proyectobackend.model.TipoChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUsuarioIdOrderByFechaUltimoMensajeDesc(Long usuarioId);
    Optional<Chat> findByUsuarioIdAndTipoChat(Long usuarioId, TipoChat tipoChat);
}

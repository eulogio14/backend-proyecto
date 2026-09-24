package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.ChatResponseDto;
import com.example.proyectobackend.dto.EnviarMensajeDto;
import com.example.proyectobackend.dto.MensajeResponseDto;
import com.example.proyectobackend.exception.ForbiddenException;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.*;
import com.example.proyectobackend.repository.ChatRepository;
import com.example.proyectobackend.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioService usuarioService;
    private final SimuladorExIaService simuladorExIaService;
    private final DtoMapper dtoMapper;

    @Transactional
    public ChatResponseDto obtenerOCrearChat(String email, TipoChat tipoChat) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        Chat chat = chatRepository.findByUsuarioIdAndTipoChat(usuario.getId(), tipoChat)
                .orElseGet(() -> {
                    Chat nuevoChat = Chat.builder()
                            .usuario(usuario)
                            .tipoChat(tipoChat)
                            .titulo(tipoChat == TipoChat.CHAT_EX ? usuario.getNombreEx() : "Asistente de Superación")
                            .ultimoMensaje("Chat iniciado")
                            .fechaUltimoMensaje(LocalDateTime.now())
                            .build();
                    return chatRepository.save(nuevoChat);
                });

        List<Mensaje> mensajes = mensajeRepository.findByChatIdOrderByTimestampAsc(chat.getId());
        return dtoMapper.toChatResponseDto(chat, mensajes);
    }

    @Transactional(readOnly = true)
    public List<ChatResponseDto> listarChatsUsuario(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        List<Chat> chats = chatRepository.findByUsuarioIdOrderByFechaUltimoMensajeDesc(usuario.getId());

        return chats.stream().map(c -> {
            List<Mensaje> mensajes = mensajeRepository.findByChatIdOrderByTimestampAsc(c.getId());
            return dtoMapper.toChatResponseDto(c, mensajes);
        }).collect(Collectors.toList());
    }

    @Transactional
    public MensajeResponseDto enviarMensaje(String email, Long chatId, EnviarMensajeDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat no encontrado con id: " + chatId));

        if (!chat.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tienes permisos para interactuar en este chat");
        }

        // 1. Guardar mensaje del usuario
        Mensaje mensajeUsuario = Mensaje.builder()
                .chat(chat)
                .remitente(Remitente.USUARIO)
                .contenido(dto.getContenido())
                .estado(EstadoMensaje.LEIDO)
                .timestamp(LocalDateTime.now())
                .build();
        mensajeRepository.save(mensajeUsuario);

        // 2. Generar respuesta de IA según modo y personalidad
        String respuestaTexto = simuladorExIaService.generarRespuesta(
                usuario,
                dto.getContenido(),
                usuario.getModoActual()
        );

        // 3. Guardar mensaje del bot (Ex o Apoyo)
        Mensaje mensajeBot = Mensaje.builder()
                .chat(chat)
                .remitente(chat.getTipoChat() == TipoChat.CHAT_EX ? Remitente.EX_BOT : Remitente.SISTEMA)
                .contenido(respuestaTexto)
                .estado(EstadoMensaje.LEIDO)
                .timestamp(LocalDateTime.now())
                .build();
        Mensaje guardadoBot = mensajeRepository.save(mensajeBot);

        // 4. Actualizar metadata del chat
        chat.setUltimoMensaje(respuestaTexto);
        chat.setFechaUltimoMensaje(LocalDateTime.now());
        chatRepository.save(chat);

        return dtoMapper.toMensajeResponseDto(guardadoBot);
    }

    @Transactional
    public void limpiarHistorial(String email, Long chatId) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat no encontrado con id: " + chatId));

        if (!chat.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tienes permisos para modificar este chat");
        }

        List<Mensaje> mensajes = mensajeRepository.findByChatIdOrderByTimestampAsc(chatId);
        mensajeRepository.deleteAll(mensajes);

        chat.setUltimoMensaje("Historial reiniciado");
        chat.setFechaUltimoMensaje(LocalDateTime.now());
        chatRepository.save(chat);
    }
}

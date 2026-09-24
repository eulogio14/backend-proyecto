package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.ChatResponseDto;
import com.example.proyectobackend.dto.EnviarMensajeDto;
import com.example.proyectobackend.dto.MensajeResponseDto;
import com.example.proyectobackend.model.TipoChat;
import com.example.proyectobackend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatResponseDto>> listarChats(Authentication auth) {
        return ResponseEntity.ok(chatService.listarChatsUsuario(auth.getName()));
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<ChatResponseDto> obtenerChatPorTipo(Authentication auth,
                                                              @PathVariable String tipo) {
        TipoChat tipoChat = (tipo.equalsIgnoreCase("ex") || tipo.equalsIgnoreCase("chat_ex"))
                ? TipoChat.CHAT_EX : TipoChat.CHAT_APOYO;
        return ResponseEntity.ok(chatService.obtenerOCrearChat(auth.getName(), tipoChat));
    }

    @PostMapping("/{chatId}/mensajes")
    public ResponseEntity<MensajeResponseDto> enviarMensaje(Authentication auth,
                                                            @PathVariable Long chatId,
                                                            @Valid @RequestBody EnviarMensajeDto dto) {
        MensajeResponseDto respuestaBot = chatService.enviarMensaje(auth.getName(), chatId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaBot);
    }

    @DeleteMapping("/{chatId}/mensajes")
    public ResponseEntity<Void> limpiarHistorial(Authentication auth,
                                                 @PathVariable Long chatId) {
        chatService.limpiarHistorial(auth.getName(), chatId);
        return ResponseEntity.noContent().build();
    }
}

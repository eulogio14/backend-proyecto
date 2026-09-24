package com.example.proyectobackend.event;

import com.example.proyectobackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppEventListener {

    private final EmailService emailService;

    @Async("taskExecutor")
    @EventListener
    public void manejarUsuarioRegistrado(UsuarioRegistradoEvent event) {
        log.info("[EVENTO] Nuevo usuario registrado: {} ({})", event.getUsuario().getNombre(), event.getUsuario().getEmail());
        emailService.enviarEmailBienvenida(event.getUsuario().getEmail(), event.getUsuario().getNombre());
    }

    @Async("taskExecutor")
    @EventListener
    public void manejarRecaidaRegistrada(RecaidaRegistradaEvent event) {
        log.info("[EVENTO] Recaída registrada para el usuario {}: {}", event.getUsuario().getEmail(), event.getRecaida().getMotivo());
        emailService.enviarAlertaRecaida(
                event.getUsuario().getEmail(),
                event.getUsuario().getNombre(),
                event.getRecaida().getMotivo()
        );
    }
}

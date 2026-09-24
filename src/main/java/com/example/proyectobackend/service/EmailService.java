package com.example.proyectobackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:contacto@contactocero.app}")
    private String remitenteEmail;

    @Async("taskExecutor")
    public void enviarEmailBienvenida(String destinatario, String nombreUsuario) {
        String asunto = "¡Bienvenido a Contacto Cero & Superación!";
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", nombreUsuario != null ? nombreUsuario : "Usuario");

            String contenidoHtml = templateEngine.process("email-bienvenida", context);
            enviarHtml(destinatario, asunto, contenidoHtml);
        } catch (Exception e) {
            log.error("Error al procesar plantilla Thymeleaf para email de bienvenida: {}", e.getMessage());
            // Fallback resiliente
            String fallbackHtml = String.format("<h2>Hola %s</h2><p>Bienvenido a Contacto Cero.</p>", nombreUsuario);
            enviarHtml(destinatario, asunto, fallbackHtml);
        }
    }

    @Async("taskExecutor")
    public void enviarAlertaRecaida(String destinatario, String nombreUsuario, String motivo) {
        String asunto = "Alerta de Recaída: No te rindas, un tropiezo no es el final";
        try {
            Context context = new Context();
            context.setVariable("nombreUsuario", nombreUsuario != null ? nombreUsuario : "Usuario");
            context.setVariable("motivo", motivo != null ? motivo : "Disparador emocional no especificado");

            String contenidoHtml = templateEngine.process("email-recaida", context);
            enviarHtml(destinatario, asunto, contenidoHtml);
        } catch (Exception e) {
            log.error("Error al procesar plantilla Thymeleaf para email de recaída: {}", e.getMessage());
            // Fallback resiliente
            String fallbackHtml = String.format("<h2>Alerta de Recaída</h2><p>Hola %s, motivo: %s</p>", nombreUsuario, motivo);
            enviarHtml(destinatario, asunto, fallbackHtml);
        }
    }

    private void enviarHtml(String destinatario, String asunto, String contenidoHtml) {
        if (mailSender == null) {
            log.warn("[EMAIL SIMULADO] Servidor SMTP no configurado. Simulación de envío a: {}, Asunto: {}", destinatario, asunto);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitenteEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", destinatario);
        } catch (MessagingException e) {
            log.error("Error al construir o enviar correo electrónico a {}: {}", destinatario, e.getMessage());
        } catch (Exception e) {
            log.error("Excepción inesperada en el servicio de correo: {}", e.getMessage());
        }
    }
}

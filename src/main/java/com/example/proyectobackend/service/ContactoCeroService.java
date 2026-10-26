package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.EstadisticasContactoCeroDto;
import com.example.proyectobackend.dto.RecaidaDto;
import com.example.proyectobackend.dto.RecaidaResponseDto;
import com.example.proyectobackend.event.RecaidaRegistradaEvent;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.ContactoCeroRecaida;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.ContactoCeroRecaidaRepository;
import com.example.proyectobackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactoCeroService {

    private final ContactoCeroRecaidaRepository recaidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final ApplicationEventPublisher eventPublisher;
    private final DtoMapper dtoMapper;

    @Transactional
    public RecaidaResponseDto registrarRecaida(String email, RecaidaDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        int diasAlcanzados = calcularDiasActuales(usuario);


        usuario.setFechaInicioContactoCero(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Guardar registro histórico de la recaída
        ContactoCeroRecaida recaida = ContactoCeroRecaida.builder()
                .usuario(usuario)
                .diasAlcanzados(diasAlcanzados)
                .motivo(dto.getMotivo())
                .fechaRecaida(LocalDateTime.now())
                .build();
        ContactoCeroRecaida guardada = recaidaRepository.save(recaida);

        // Publicar evento asíncrono para alerta y correo de apoyo
        eventPublisher.publishEvent(new RecaidaRegistradaEvent(this, usuario, guardada));

        return dtoMapper.toRecaidaResponseDto(guardada);
    }



    @Transactional(readOnly = true)
    public List<RecaidaResponseDto> listarRecaidas(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        return recaidaRepository.findByUsuarioIdOrderByFechaRecaidaDesc(usuario.getId())
                .stream()
                .map(dtoMapper::toRecaidaResponseDto)
                .collect(Collectors.toList());
    }

    private int calcularDiasActuales(Usuario usuario) {
        if (usuario.getFechaInicioContactoCero() == null) {
            usuario.setFechaInicioContactoCero(LocalDateTime.now());
            return 0;
        }
        long dias = Duration.between(usuario.getFechaInicioContactoCero(), LocalDateTime.now()).toDays();
        return (int) Math.max(0, dias);
    }
}

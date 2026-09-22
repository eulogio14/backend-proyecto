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

        // Actualizar racha máxima si la racha actual la supera
        if (diasAlcanzados > usuario.getRachaMaximaContactoCero()) {
            usuario.setRachaMaximaContactoCero(diasAlcanzados);
        }

        // Reiniciar contador de contacto cero
        usuario.setDiasRachaContactoCero(0);
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

    @Transactional
    public EstadisticasContactoCeroDto obtenerEstadisticas(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        int diasActuales = calcularDiasActuales(usuario);

        if (diasActuales != usuario.getDiasRachaContactoCero()) {
            usuario.setDiasRachaContactoCero(diasActuales);
            if (diasActuales > usuario.getRachaMaximaContactoCero()) {
                usuario.setRachaMaximaContactoCero(diasActuales);
            }
            usuarioRepository.save(usuario);
        }

        long totalRecaidas = recaidaRepository.countByUsuarioId(usuario.getId());

        return EstadisticasContactoCeroDto.builder()
                .diasRachaActual(usuario.getDiasRachaContactoCero())
                .rachaMaxima(usuario.getRachaMaximaContactoCero())
                .totalRecaidas(totalRecaidas)
                .fechaInicioContactoCero(usuario.getFechaInicioContactoCero())
                .build();
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

package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.DiarioDto;
import com.example.proyectobackend.dto.DiarioResponseDto;
import com.example.proyectobackend.exception.ForbiddenException;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.DiarioEntrada;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.DiarioEntradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiarioService {

    private final DiarioEntradaRepository diarioRepository;
    private final UsuarioService usuarioService;
    private final DtoMapper dtoMapper;

    @Transactional
    public DiarioResponseDto crearEntrada(String email, DiarioDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        DiarioEntrada entrada = DiarioEntrada.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo())
                .contenido(dto.getContenido())
                .estadoAnimo(dto.getEstadoAnimo() != null ? dto.getEstadoAnimo() : "EN_PROCESO")
                .fecha(LocalDateTime.now())
                .build();

        DiarioEntrada guardada = diarioRepository.save(entrada);
        return dtoMapper.toDiarioResponseDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<DiarioResponseDto> listarEntradas(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        return diarioRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId())
                .stream()
                .map(dtoMapper::toDiarioResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarEntrada(String email, Long id) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        DiarioEntrada entrada = diarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de diario no encontrada con id: " + id));

        if (!entrada.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tienes permisos para eliminar esta entrada de diario");
        }

        diarioRepository.delete(entrada);
    }
}

package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.RecuerdoDto;
import com.example.proyectobackend.dto.RecuerdoResponseDto;
import com.example.proyectobackend.exception.ForbiddenException;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.Recuerdo;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.RecuerdoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecuerdoService {

    private final RecuerdoRepository recuerdoRepository;
    private final UsuarioService usuarioService;
    private final DtoMapper dtoMapper;

    @Transactional
    public RecuerdoResponseDto crearRecuerdo(String email, RecuerdoDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        Recuerdo recuerdo = Recuerdo.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .urlFoto(dto.getUrlFoto())
                .fechaRecuerdo(dto.getFechaRecuerdo())
                .nivelNostalgia(dto.getNivelNostalgia())
                .tipo(dto.getTipo() != null ? dto.getTipo() : "FOTO")
                .fechaCreacion(LocalDateTime.now())
                .build();

        Recuerdo guardado = recuerdoRepository.save(recuerdo);
        return dtoMapper.toRecuerdoResponseDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<RecuerdoResponseDto> listarRecuerdos(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        return recuerdoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(dtoMapper::toRecuerdoResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarRecuerdo(String email, Long id) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        Recuerdo recuerdo = recuerdoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recuerdo no encontrado con id: " + id));

        if (!recuerdo.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tienes permisos para eliminar este recuerdo");
        }

        recuerdoRepository.delete(recuerdo);
    }
}

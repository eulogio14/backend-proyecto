package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.RedFlagDto;
import com.example.proyectobackend.dto.RedFlagResponseDto;
import com.example.proyectobackend.exception.ForbiddenException;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.RedFlag;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.RedFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedFlagService {

    private final RedFlagRepository redFlagRepository;
    private final UsuarioService usuarioService;
    private final DtoMapper dtoMapper;

    @Transactional
    public RedFlagResponseDto crearRedFlag(String email, RedFlagDto dto) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);

        RedFlag redFlag = RedFlag.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .gravedad(dto.getGravedad())
                .fechaRegistro(LocalDateTime.now())
                .build();

        RedFlag guardada = redFlagRepository.save(redFlag);
        return dtoMapper.toRedFlagResponseDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<RedFlagResponseDto> listarRedFlags(String email) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        return redFlagRepository.findByUsuarioIdOrderByGravedadDesc(usuario.getId())
                .stream()
                .map(dtoMapper::toRedFlagResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarRedFlag(String email, Long id) {
        Usuario usuario = usuarioService.encontrarPorEmail(email);
        RedFlag redFlag = redFlagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Red Flag no encontrada con id: " + id));

        if (!redFlag.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tienes permisos para eliminar este Red Flag");
        }

        redFlagRepository.delete(redFlag);
    }
}

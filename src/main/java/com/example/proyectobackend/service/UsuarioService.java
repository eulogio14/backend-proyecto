package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.CambiarModoDto;
import com.example.proyectobackend.dto.ConfigExDto;
import com.example.proyectobackend.dto.UsuarioResponseDto;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPerfil(String email) {
        Usuario usuario = encontrarPorEmail(email);
        return dtoMapper.toUsuarioResponseDto(usuario);
    }

    @Transactional
    public UsuarioResponseDto actualizarConfigEx(String email, ConfigExDto dto) {
        Usuario usuario = encontrarPorEmail(email);

        if (dto.getNombreEx() != null && !dto.getNombreEx().isBlank()) {
            usuario.setNombreEx(dto.getNombreEx());
        }
        if (dto.getApodoEx() != null) {
            usuario.setApodoEx(dto.getApodoEx());
        }
        if (dto.getFotoPerfilEx() != null) {
            usuario.setFotoPerfilEx(dto.getFotoPerfilEx());
        }
        if (dto.getEstadoWhatsAppEx() != null) {
            usuario.setEstadoWhatsAppEx(dto.getEstadoWhatsAppEx());
        }
        if (dto.getPersonalidadEx() != null) {
            usuario.setPersonalidadEx(dto.getPersonalidadEx());
        }
        if (dto.getFechaRuptura() != null) {
            usuario.setFechaRuptura(dto.getFechaRuptura());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return dtoMapper.toUsuarioResponseDto(actualizado);
    }

    @Transactional
    public UsuarioResponseDto cambiarModo(String email, CambiarModoDto dto) {
        Usuario usuario = encontrarPorEmail(email);
        usuario.setModoActual(dto.getModo());
        Usuario actualizado = usuarioRepository.save(usuario);
        return dtoMapper.toUsuarioResponseDto(actualizado);
    }

    public Usuario encontrarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}

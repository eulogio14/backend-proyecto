package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.AnuncioConfigDto;
import com.example.proyectobackend.dto.MetricasGlobalesDto;
import com.example.proyectobackend.exception.DuplicateResourceException;
import com.example.proyectobackend.exception.ResourceNotFoundException;
import com.example.proyectobackend.model.AnuncioConfig;
import com.example.proyectobackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final ContactoCeroRecaidaRepository recaidaRepository;
    private final DiarioEntradaRepository diarioRepository;
    private final RedFlagRepository redFlagRepository;
    private final MensajeRepository mensajeRepository;
    private final AnuncioConfigRepository anuncioConfigRepository;

    @Transactional(readOnly = true)
    public MetricasGlobalesDto obtenerMetricasGlobales() {
        return MetricasGlobalesDto.builder()
                .totalUsuarios(usuarioRepository.count())
                .totalRecaidasRegistradas(recaidaRepository.count())
                .totalEntradasDiario(diarioRepository.count())
                .totalRedFlags(redFlagRepository.count())
                .totalMensajesIntercambiados(mensajeRepository.count())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AnuncioConfigDto> listarTodosLosAnuncios() {
        return anuncioConfigRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnuncioConfigDto> listarAnunciosActivos() {
        return anuncioConfigRepository.findByActivoTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnuncioConfigDto crearAnuncio(AnuncioConfigDto dto) {
        if (anuncioConfigRepository.findByClave(dto.getClave()).isPresent()) {
            throw new DuplicateResourceException("Ya existe una configuración con la clave: " + dto.getClave());
        }

        AnuncioConfig config = AnuncioConfig.builder()
                .clave(dto.getClave().trim().toUpperCase())
                .titulo(dto.getTitulo())
                .adUnitId(dto.getAdUnitId())
                .activo(dto.isActivo())
                .frecuenciaMensajes(dto.getFrecuenciaMensajes() > 0 ? dto.getFrecuenciaMensajes() : 5)
                .descripcion(dto.getDescripcion())
                .build();

        return toDto(anuncioConfigRepository.save(config));
    }

    @Transactional
    public AnuncioConfigDto alternarEstado(Long id) {
        AnuncioConfig config = anuncioConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de anuncio no encontrada con id: " + id));

        config.setActivo(!config.isActivo());
        return toDto(anuncioConfigRepository.save(config));
    }

    private AnuncioConfigDto toDto(AnuncioConfig config) {
        return AnuncioConfigDto.builder()
                .id(config.getId())
                .clave(config.getClave())
                .titulo(config.getTitulo())
                .adUnitId(config.getAdUnitId())
                .activo(config.isActivo())
                .frecuenciaMensajes(config.getFrecuenciaMensajes())
                .descripcion(config.getDescripcion())
                .build();
    }
}

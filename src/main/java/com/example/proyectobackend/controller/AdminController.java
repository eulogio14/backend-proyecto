package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.AnuncioConfigDto;
import com.example.proyectobackend.dto.MetricasGlobalesDto;
import com.example.proyectobackend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Endpoints con autorización estricta para administradores")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Obtener métricas globales del sistema", description = "Requiere rol ROLE_ADMIN")
    @GetMapping("/metricas-globales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricasGlobalesDto> obtenerMetricasGlobales() {
        return ResponseEntity.ok(adminService.obtenerMetricasGlobales());
    }

    @Operation(summary = "Listar todas las configuraciones de anuncios", description = "Requiere rol ROLE_ADMIN")
    @GetMapping("/anuncios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnuncioConfigDto>> listarTodosLosAnuncios() {
        return ResponseEntity.ok(adminService.listarTodosLosAnuncios());
    }

    @Operation(summary = "Crear nueva configuración de anuncio o banner", description = "Requiere rol ROLE_ADMIN")
    @PostMapping("/anuncios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnuncioConfigDto> crearAnuncio(@Valid @RequestBody AnuncioConfigDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.crearAnuncio(dto));
    }

    @Operation(summary = "Activar o desactivar anuncio por ID", description = "Requiere rol ROLE_ADMIN")
    @PatchMapping("/anuncios/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnuncioConfigDto> alternarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.alternarEstado(id));
    }

    @Operation(summary = "Listar anuncios activos para clientes móviles o web")
    @GetMapping("/anuncios/activos")
    public ResponseEntity<List<AnuncioConfigDto>> listarAnunciosActivos() {
        return ResponseEntity.ok(adminService.listarAnunciosActivos());
    }
}

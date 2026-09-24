package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.*;
import com.example.proyectobackend.service.ExDatasetService;
import com.example.proyectobackend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios & Perfil", description = "Gestión de perfil, modos y configuración del clon de IA")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ExDatasetService exDatasetService;

    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponseDto> obtenerPerfil(Authentication auth) {
        UsuarioResponseDto perfil = usuarioService.obtenerPerfil(auth.getName());
        return ResponseEntity.ok(perfil);
    }

    @Operation(summary = "Actualizar configuración del perfil del ex (apariencia WhatsApp)")
    @PutMapping("/config-ex")
    public ResponseEntity<UsuarioResponseDto> actualizarConfigEx(Authentication auth,
                                                                 @RequestBody ConfigExDto dto) {
        UsuarioResponseDto actualizado = usuarioService.actualizarConfigEx(auth.getName(), dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Cambiar modo de la aplicación (RECORDAR o SUPERAR)")
    @PatchMapping("/modo")
    public ResponseEntity<UsuarioResponseDto> cambiarModo(Authentication auth,
                                                          @Valid @RequestBody CambiarModoDto dto) {
        UsuarioResponseDto actualizado = usuarioService.cambiarModo(auth.getName(), dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Subir y entrenar dataset de chat del ex (Inspirado en JustZando)",
               description = "Procesa el historial de chat exportado de WhatsApp o redes sociales, detecta el tono, emojis y modismos del ex.")
    @PostMapping("/ex-dataset")
    public ResponseEntity<ExDatasetAnalisisDto> subirDatasetEx(Authentication auth,
                                                              @Valid @RequestBody ExDatasetUploadDto dto) {
        ExDatasetAnalisisDto resultado = exDatasetService.procesarYEntrenarDataset(auth.getName(), dto);
        return ResponseEntity.ok(resultado);
    }
}

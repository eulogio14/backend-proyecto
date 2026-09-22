package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.DiarioDto;
import com.example.proyectobackend.dto.DiarioResponseDto;
import com.example.proyectobackend.service.DiarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diarios")
@RequiredArgsConstructor
public class DiarioController {

    private final DiarioService diarioService;

    @GetMapping
    public ResponseEntity<List<DiarioResponseDto>> listarEntradas(Authentication auth) {
        return ResponseEntity.ok(diarioService.listarEntradas(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<DiarioResponseDto> crearEntrada(Authentication auth,
                                                          @Valid @RequestBody DiarioDto dto) {
        DiarioResponseDto guardada = diarioService.crearEntrada(auth.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEntrada(Authentication auth,
                                                @PathVariable Long id) {
        diarioService.eliminarEntrada(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

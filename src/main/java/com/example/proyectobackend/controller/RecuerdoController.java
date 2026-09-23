package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.RecuerdoDto;
import com.example.proyectobackend.dto.RecuerdoResponseDto;
import com.example.proyectobackend.service.RecuerdoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recuerdos")
@RequiredArgsConstructor
public class RecuerdoController {

    private final RecuerdoService recuerdoService;

    @GetMapping
    public ResponseEntity<List<RecuerdoResponseDto>> listarRecuerdos(Authentication auth) {
        return ResponseEntity.ok(recuerdoService.listarRecuerdos(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<RecuerdoResponseDto> crearRecuerdo(Authentication auth,
                                                             @Valid @RequestBody RecuerdoDto dto) {
        RecuerdoResponseDto guardado = recuerdoService.crearRecuerdo(auth.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecuerdo(Authentication auth,
                                                 @PathVariable Long id) {
        recuerdoService.eliminarRecuerdo(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

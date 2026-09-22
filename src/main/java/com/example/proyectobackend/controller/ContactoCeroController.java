package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.EstadisticasContactoCeroDto;
import com.example.proyectobackend.dto.RecaidaDto;
import com.example.proyectobackend.dto.RecaidaResponseDto;
import com.example.proyectobackend.service.ContactoCeroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacto-cero")
@RequiredArgsConstructor
public class ContactoCeroController {

    private final ContactoCeroService contactoCeroService;

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasContactoCeroDto> obtenerEstadisticas(Authentication auth) {
        return ResponseEntity.ok(contactoCeroService.obtenerEstadisticas(auth.getName()));
    }

    @GetMapping("/recaidas")
    public ResponseEntity<List<RecaidaResponseDto>> listarRecaidas(Authentication auth) {
        return ResponseEntity.ok(contactoCeroService.listarRecaidas(auth.getName()));
    }

    @PostMapping("/recaidas")
    public ResponseEntity<RecaidaResponseDto> registrarRecaida(Authentication auth,
                                                               @Valid @RequestBody RecaidaDto dto) {
        RecaidaResponseDto respuesta = contactoCeroService.registrarRecaida(auth.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}

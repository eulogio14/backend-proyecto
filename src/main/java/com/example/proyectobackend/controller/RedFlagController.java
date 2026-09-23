package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.RedFlagDto;
import com.example.proyectobackend.dto.RedFlagResponseDto;
import com.example.proyectobackend.service.RedFlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/red-flags")
@RequiredArgsConstructor
public class RedFlagController {

    private final RedFlagService redFlagService;

    @GetMapping
    public ResponseEntity<List<RedFlagResponseDto>> listarRedFlags(Authentication auth) {
        return ResponseEntity.ok(redFlagService.listarRedFlags(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<RedFlagResponseDto> crearRedFlag(Authentication auth,
                                                           @Valid @RequestBody RedFlagDto dto) {
        RedFlagResponseDto guardada = redFlagService.crearRedFlag(auth.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRedFlag(Authentication auth,
                                                @PathVariable Long id) {
        redFlagService.eliminarRedFlag(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

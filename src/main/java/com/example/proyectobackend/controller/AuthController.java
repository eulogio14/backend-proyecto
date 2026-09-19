package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.GoogleTokenDto;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns= "*") // Fundamental para permitir peticiones desde la IP de tu celular
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> loginConGoogle(@RequestBody GoogleTokenDto googleTokenDto) {
        try {
            Usuario usuario = authService.verificarYLoguearConGoogle(googleTokenDto.getToken());

            // Si es exitoso, devuelve status 200 y los datos del usuario en formato JSON
            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            // Si el token es falso o expirado, devuelve status 401 y el mensaje de error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error en autenticación: " + e.getMessage());
        }
    }
}

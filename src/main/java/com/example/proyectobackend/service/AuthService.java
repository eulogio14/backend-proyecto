package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.*;
import com.example.proyectobackend.event.UsuarioRegistradoEvent;
import com.example.proyectobackend.exception.DuplicateResourceException;
import com.example.proyectobackend.exception.UnauthorizedException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.ModoApp;
import com.example.proyectobackend.model.Rol;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import com.example.proyectobackend.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final DtoMapper dtoMapper;

    private static final String GOOGLE_CLIENT_ID = "962526435206-8gnivbn68h8ntc10e9k6epaoe68h0o42.apps.googleusercontent.com";

    @Transactional
    public AuthResponseDto registrar(RegistroRequestDto request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("El correo ya se encuentra registrado: " + request.getEmail());
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.ROLE_USER)
                .modoActual(ModoApp.RECORDAR)
                .nombreEx(request.getNombreEx() != null ? request.getNombreEx() : "Mi Ex")
                .fotoPerfilEx(request.getFotoPerfilEx())
                .fechaInicioContactoCero(LocalDateTime.now())
                .fechaRegistro(LocalDateTime.now())
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        // Disparar evento asíncrono para bienvenida / confirmación por email
        eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, guardado));

        String accessToken = jwtService.generarAccessToken(guardado);
        String refreshToken = jwtService.generarRefreshToken(guardado);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .usuario(dtoMapper.toUsuarioResponseDto(guardado))
                .build();
    }

    public AuthResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Credenciales incorrectas: correo o contraseña inválidos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshToken = jwtService.generarRefreshToken(usuario);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .usuario(dtoMapper.toUsuarioResponseDto(usuario))
                .build();
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        String email = jwtService.extraerEmail(request.getRefreshToken());
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario asociado al token no existe"));

        String newAccessToken = jwtService.generarAccessToken(usuario);
        String newRefreshToken = jwtService.generarRefreshToken(usuario);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .usuario(dtoMapper.toUsuarioResponseDto(usuario))
                .build();
    }

    @Transactional
    public AuthResponseDto loginConGoogle(String tokenGoogle) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenGoogle);

            if (idToken == null) {
                throw new UnauthorizedException("Token de Google inválido o caducado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().toLowerCase().trim();
            String nombre = (String) payload.get("name");

            Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
                Usuario nuevo = Usuario.builder()
                        .email(email)
                        .nombre(nombre != null ? nombre : "Usuario Google")
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .rol(Rol.ROLE_USER)
                        .modoActual(ModoApp.RECORDAR)
                        .fechaInicioContactoCero(LocalDateTime.now())
                        .fechaRegistro(LocalDateTime.now())
                        .build();
                Usuario registrado = usuarioRepository.save(nuevo);
                eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, registrado));
                return registrado;
            });

            String accessToken = jwtService.generarAccessToken(usuario);
            String refreshToken = jwtService.generarRefreshToken(usuario);

            return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .usuario(dtoMapper.toUsuarioResponseDto(usuario))
                .build();

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al autenticar con Google: {}", e.getMessage());
            throw new UnauthorizedException("Fallo en la verificación con los servidores de Google: " + e.getMessage());
        }
    }
}

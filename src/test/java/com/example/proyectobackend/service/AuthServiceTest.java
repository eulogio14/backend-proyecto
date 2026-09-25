package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.AuthResponseDto;
import com.example.proyectobackend.dto.LoginRequestDto;
import com.example.proyectobackend.dto.RegistroRequestDto;
import com.example.proyectobackend.exception.DuplicateResourceException;
import com.example.proyectobackend.exception.UnauthorizedException;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.Rol;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import com.example.proyectobackend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("test@contactocero.app")
                .nombre("Rick Sanchez")
                .password("encoded_pass")
                .rol(Rol.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Registro exitoso genera tokens y publica evento de bienvenida")
    void testRegistrarExitoso() {
        RegistroRequestDto request = new RegistroRequestDto();
        request.setNombre("Rick Sanchez");
        request.setEmail("rick@c137.com");
        request.setPassword("Password123!");
        request.setNombreEx("Diane");

        when(usuarioRepository.existsByEmail("rick@c137.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_secret");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generarAccessToken(any(Usuario.class))).thenReturn("fake_access_token");
        when(jwtService.generarRefreshToken(any(Usuario.class))).thenReturn("fake_refresh_token");

        AuthResponseDto response = authService.registrar(request);

        assertNotNull(response);
        assertEquals("fake_access_token", response.getAccessToken());
        assertEquals("fake_refresh_token", response.getRefreshToken());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Registro con correo duplicado lanza DuplicateResourceException")
    void testRegistrarEmailDuplicado() {
        RegistroRequestDto request = new RegistroRequestDto();
        request.setEmail("test@contactocero.app");

        when(usuarioRepository.existsByEmail("test@contactocero.app")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login con credenciales incorrectas lanza UnauthorizedException")
    void testLoginCredencialesInvalidas() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@contactocero.app");
        loginRequest.setPassword("wrong_password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }
}

package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.EstadisticasContactoCeroDto;
import com.example.proyectobackend.dto.RecaidaDto;
import com.example.proyectobackend.dto.RecaidaResponseDto;
import com.example.proyectobackend.mapper.DtoMapper;
import com.example.proyectobackend.model.ContactoCeroRecaida;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.ContactoCeroRecaidaRepository;
import com.example.proyectobackend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactoCeroServiceTest {

    @Mock
    private ContactoCeroRecaidaRepository recaidaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private ContactoCeroService contactoCeroService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("test@contactocero.app")
                .nombre("Usuario Test")
                .diasRachaContactoCero(10)
                .rachaMaximaContactoCero(15)
                .fechaInicioContactoCero(LocalDateTime.now().minusDays(5))
                .build();
    }

    @Test
    @DisplayName("Registrar recaída reinicia la racha actual a 0 y emite evento")
    void testRegistrarRecaida() {
        RecaidaDto recaidaDto = new RecaidaDto("Miré sus historias de Instagram en la madrugada");

        when(usuarioService.encontrarPorEmail("test@contactocero.app")).thenReturn(usuario);
        when(recaidaRepository.save(any(ContactoCeroRecaida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecaidaResponseDto expectedResponse = RecaidaResponseDto.builder()
                .id(100L)
                .motivo(recaidaDto.getMotivo())
                .fechaRecaida(LocalDateTime.now())
                .build();
        when(dtoMapper.toRecaidaResponseDto(any(ContactoCeroRecaida.class))).thenReturn(expectedResponse);

        RecaidaResponseDto resultado = contactoCeroService.registrarRecaida("test@contactocero.app", recaidaDto);

        assertNotNull(resultado);
        assertEquals(0, usuario.getDiasRachaContactoCero(), "La racha del usuario debe reiniciarse a 0");
        verify(usuarioRepository, times(1)).save(usuario);
        verify(recaidaRepository, times(1)).save(any(ContactoCeroRecaida.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Obtener estadísticas calcula rachas y total de recaídas correctamente")
    void testObtenerEstadisticas() {
        when(usuarioService.encontrarPorEmail("test@contactocero.app")).thenReturn(usuario);
        when(recaidaRepository.countByUsuarioId(1L)).thenReturn(2L);

        EstadisticasContactoCeroDto stats = contactoCeroService.obtenerEstadisticas("test@contactocero.app");

        assertNotNull(stats);
        assertEquals(15, stats.getRachaMaxima());
        assertEquals(2L, stats.getTotalRecaidas());
    }
}

package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.ExDatasetAnalisisDto;
import com.example.proyectobackend.dto.ExDatasetUploadDto;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExDatasetServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ExDatasetService exDatasetService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("test@contactocero.app")
                .nombre("Eulogio")
                .nombreEx("Camila")
                .personalidadEx("NOSTALGICA_DISTANTE")
                .build();
    }

    @Test
    @DisplayName("Procesar chat exportado estilo JustZando extrae métricas y afina personalidad")
    void testProcesarYEntrenarDataset() {
        String chatExport = """
                [12/05/24, 22:10] Eulogio: Hola Camila, ¿cómo estás?
                [12/05/24, 22:11] Camila: Hola. Ocupada. 👍
                [12/05/24, 22:12] Eulogio: Solo quería saber de ti...
                [12/05/24, 22:15] Camila: Ya no me escribas. Ok. 🙄
                """;

        ExDatasetUploadDto uploadDto = ExDatasetUploadDto.builder()
                .contenidoChat(chatExport)
                .nombreRemitenteEx("Camila")
                .build();

        when(usuarioService.encontrarPorEmail("test@contactocero.app")).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        ExDatasetAnalisisDto resultado = exDatasetService.procesarYEntrenarDataset("test@contactocero.app", uploadDto);

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalMensajesEx());
        assertTrue(resultado.getTotalLineasProcesadas() >= 4);
        assertNotNull(resultado.getTonoDetectado());
        verify(usuarioRepository, times(1)).save(usuario);
        assertNotNull(usuario.getDatasetEntrenamientoEx());
    }
}

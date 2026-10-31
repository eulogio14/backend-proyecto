package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.*;
import com.example.proyectobackend.model.ExDatasetTask;
import com.example.proyectobackend.model.ExDatasetTaskStatus;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.ExDatasetTaskRepository;
import com.example.proyectobackend.repository.UsuarioRepository;
import com.example.proyectobackend.service.ArchivoEnMemoria;
import com.example.proyectobackend.service.ExDatasetQueueService;
import com.example.proyectobackend.service.ExDatasetService;
import com.example.proyectobackend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios & Perfil", description = "Gestión de perfil, modos y configuración del clon de IA")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ExDatasetService exDatasetService;
    private final ExDatasetQueueService exDatasetQueueService;
    private final UsuarioRepository usuarioRepository;
    private final ExDatasetTaskRepository exDatasetTaskRepository;

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

    @Operation(summary = "Cambiar modo de la aplicación (RECORDAR)")
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

    @Operation(summary = "Entrenar al Ex subiendo capturas de WhatsApp (asíncrono)",
               description = "Recibe una o varias imágenes, las pone en cola para procesar una a una "
                       + "y devuelve 202 Accepted con taskId para hacer polling.")
    @PostMapping(value = "/ex-dataset/imagenes",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirImagenesDatasetEx(
            Authentication auth,
            @RequestParam("imagenes") List<MultipartFile> imagenes,
            @RequestParam(value = "nombreRemitenteEx", required = false) String nombreRemitenteEx) {
        if (imagenes == null || imagenes.isEmpty()) {
            return ResponseEntity.badRequest().body("Debes enviar al menos una imagen");
        }

        Usuario usuario = usuarioService.encontrarPorEmail(auth.getName());

        List<MultipartFile> imagenesEnMemoria = new ArrayList<>(imagenes.size());
        try {
            for (MultipartFile f : imagenes) {
                if (f == null || f.isEmpty()) {
                    return ResponseEntity.badRequest().body("Una de las imágenes está vacía");
                }
                byte[] bytes = f.getBytes();
                if (bytes == null || bytes.length == 0) {
                    return ResponseEntity.badRequest().body("Una de las imágenes está vacía");
                }
                imagenesEnMemoria.add(new ArchivoEnMemoria(f.getOriginalFilename(), f.getContentType(), bytes));
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("No se pudo leer una de las imágenes");
        }

        ExDatasetTask tarea = ExDatasetTask.builder()
                .usuario(usuario)
                .estado(ExDatasetTaskStatus.QUEUED)
                .nombreRemitenteEx(nombreRemitenteEx)
                .creadoEn(LocalDateTime.now())
                .build();
        tarea = exDatasetTaskRepository.save(tarea);

        try {
            exDatasetQueueService.procesarTarea(tarea.getId(), usuario.getId(), nombreRemitenteEx, imagenesEnMemoria);
        } catch (Exception e) {
            tarea.setEstado(ExDatasetTaskStatus.FAILED);
            tarea.setTerminadoEn(LocalDateTime.now());
            tarea.setErrorMensaje(e.getMessage());
            tarea = exDatasetTaskRepository.save(tarea);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ExDatasetTaskResponseDto.builder()
                .taskId(tarea.getId())
                .estado(tarea.getEstado())
                .mensaje("Imágenes en cola. Usa GET /api/v1/usuarios/ex-dataset/imagenes/tareas/{taskId} para consultar el estado.")
                .creadoEn(tarea.getCreadoEn())
                .build());
    }

    @Operation(summary = "Consultar estado de tarea de entrenamiento asíncrona")
    @GetMapping("/ex-dataset/imagenes/tareas/{taskId}")
    public ResponseEntity<ExDatasetTaskResponseDto> consultarTareaDatasetEx(Authentication auth,
                                                                          @PathVariable Long taskId) {
        Usuario usuario = usuarioService.encontrarPorEmail(auth.getName());
        ExDatasetTask tarea = exDatasetTaskRepository.findById(taskId)
                .orElseThrow(() -> new com.example.proyectobackend.exception.ResourceNotFoundException("Tarea no encontrada"));

        if (!tarea.getUsuario().getId().equals(usuario.getId())) {
            throw new com.example.proyectobackend.exception.ForbiddenException("No tienes permisos para consultar esta tarea");
        }

        String mensaje = switch (tarea.getEstado()) {
            case QUEUED -> "En cola, esperando turno para procesar.";
            case PROCESSING -> "Procesando imágenes con OCR...";
            case COMPLETED -> "Completado. Dataset actualizado.";
            case FAILED -> "Falló al procesar las imágenes.";
        };

        return ResponseEntity.ok(ExDatasetTaskResponseDto.builder()
                .taskId(tarea.getId())
                .estado(tarea.getEstado())
                .mensaje(mensaje)
                .imagenesProcesadas(tarea.getImagenesProcesadas())
                .mensajesNuevos(tarea.getMensajesNuevos())
                .mensajesDuplicados(tarea.getMensajesDuplicados())
                .totalMensajesDataset(tarea.getTotalMensajesDataset())
                .totalMensajesEx(tarea.getTotalMensajesEx())
                .tonoDetectado(tarea.getTonoDetectado())
                .creadoEn(tarea.getCreadoEn())
                .iniciadoEn(tarea.getIniciadoEn())
                .terminadoEn(tarea.getTerminadoEn())
                .errorMensaje(tarea.getErrorMensaje())
                .build());
    }

    @Operation(summary = "Listar tareas de entrenamiento del usuario")
    @GetMapping("/ex-dataset/imagenes/tareas")
    public ResponseEntity<List<ExDatasetTaskResponseDto>> listarTareasDatasetEx(Authentication auth) {
        Usuario usuario = usuarioService.encontrarPorEmail(auth.getName());
        List<ExDatasetTaskResponseDto> tareas = exDatasetTaskRepository.findByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .stream()
                .map(t -> ExDatasetTaskResponseDto.builder()
                        .taskId(t.getId())
                        .estado(t.getEstado())
                        .creadoEn(t.getCreadoEn())
                        .iniciadoEn(t.getIniciadoEn())
                        .terminadoEn(t.getTerminadoEn())
                        .imagenesProcesadas(t.getImagenesProcesadas())
                        .mensajesNuevos(t.getMensajesNuevos())
                        .mensajesDuplicados(t.getMensajesDuplicados())
                        .totalMensajesDataset(t.getTotalMensajesDataset())
                        .totalMensajesEx(t.getTotalMensajesEx())
                        .tonoDetectado(t.getTonoDetectado())
                        .errorMensaje(t.getErrorMensaje())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(tareas);
    }
}

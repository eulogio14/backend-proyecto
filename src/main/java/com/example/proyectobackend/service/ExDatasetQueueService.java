package com.example.proyectobackend.service;

import com.example.proyectobackend.model.ExDatasetTask;
import com.example.proyectobackend.model.ExDatasetTaskStatus;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.ExDatasetTaskRepository;
import com.example.proyectobackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExDatasetQueueService {

    private final ExDatasetTaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExOcrService exOcrService;
    private final ExDatasetService exDatasetService;

    @Async("ocrTaskExecutor")
    @Transactional
    public void procesarTarea(Long taskId, Long usuarioId, String nombreRemitenteEx,
                              List<MultipartFile> imagenesGuardadas) {
        ExDatasetTask tarea = taskRepository.findById(taskId).orElse(null);
        if (tarea == null) {
            return;
        }
        try {
            tarea.setEstado(ExDatasetTaskStatus.PROCESSING);
            tarea.setIniciadoEn(LocalDateTime.now());
            taskRepository.save(tarea);

            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            if (usuario == null) {
                throw new IllegalStateException("Usuario no encontrado");
            }

            List<org.springframework.web.multipart.MultipartFile> archivos = new ArrayList<>();
            // Recrear archivos temporales desde bytes guardados si es necesario
            // Pero MultipartFile no es serializable; el servicio original ya procesa
            // En este flujo, procesaremos usando el servicio existente
            var resultado = exDatasetService.procesarImagenes(usuario.getEmail(), imagenesGuardadas, nombreRemitenteEx);

            tarea.setEstado(ExDatasetTaskStatus.COMPLETED);
            tarea.setTerminadoEn(LocalDateTime.now());
            tarea.setResultadoTexto(usuario.getDatasetEntrenamientoEx());
            tarea.setImagenesProcesadas(resultado.getImagenesProcesadas());
            tarea.setMensajesNuevos(resultado.getMensajesNuevos());
            tarea.setMensajesDuplicados(resultado.getMensajesDuplicados());
            tarea.setTotalMensajesDataset(resultado.getTotalMensajesDataset());
            tarea.setTotalMensajesEx(resultado.getTotalMensajesEx());
            tarea.setTonoDetectado(resultado.getTonoDetectado());
            taskRepository.save(tarea);

            log.info("Tarea OCR {} completada para usuario {}", taskId, usuarioId);
        } catch (Exception e) {
            log.error("Error procesando tarea OCR {}", taskId, e);
            tarea.setEstado(ExDatasetTaskStatus.FAILED);
            tarea.setTerminadoEn(LocalDateTime.now());
            tarea.setErrorMensaje(e.getMessage());
            taskRepository.save(tarea);
        }
    }
}

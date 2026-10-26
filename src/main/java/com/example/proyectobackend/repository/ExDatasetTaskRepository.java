package com.example.proyectobackend.repository;

import com.example.proyectobackend.model.ExDatasetTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExDatasetTaskRepository extends JpaRepository<ExDatasetTask, Long> {
    List<ExDatasetTask> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
}

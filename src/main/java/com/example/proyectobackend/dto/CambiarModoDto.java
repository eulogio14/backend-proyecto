package com.example.proyectobackend.dto;

import com.example.proyectobackend.model.ModoApp;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarModoDto {
    @NotNull(message = "El nuevo modo de aplicación es obligatorio (RECORDAR o SUPERAR)")
    private ModoApp modo;
}

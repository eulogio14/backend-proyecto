package com.example.proyectobackend.exception;

public class BadRequestException extends BaseAppException {
    public BadRequestException(String message) {
        super(message);
    }
}

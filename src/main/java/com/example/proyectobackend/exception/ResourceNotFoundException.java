package com.example.proyectobackend.exception;

public class ResourceNotFoundException extends BaseAppException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

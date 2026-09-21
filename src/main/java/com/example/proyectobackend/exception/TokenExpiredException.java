package com.example.proyectobackend.exception;

public class TokenExpiredException extends BaseAppException {
    public TokenExpiredException(String message) {
        super(message);
    }
}

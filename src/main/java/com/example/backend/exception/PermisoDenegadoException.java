package com.example.backend.exception;

public class PermisoDenegadoException extends RuntimeException {
    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}

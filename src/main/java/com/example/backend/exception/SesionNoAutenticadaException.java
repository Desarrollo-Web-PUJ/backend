package com.example.backend.exception;

public class SesionNoAutenticadaException extends RuntimeException {

    public SesionNoAutenticadaException(String mensaje) {
        super(mensaje);
    }
}
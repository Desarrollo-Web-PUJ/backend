package com.example.backend.dto;

public class EliminacionRolProcesoResponseDTO {

    private String mensaje;

    public EliminacionRolProcesoResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() { return mensaje; }
}

package com.example.backend.dto;

import java.util.List;

public class EliminacionFlujoMensajeResponseDTO {

    private String mensaje;
    private List<String> advertencias;

    public EliminacionFlujoMensajeResponseDTO(String mensaje, List<String> advertencias) {
        this.mensaje = mensaje;
        this.advertencias = advertencias;
    }

    public String getMensaje() { return mensaje; }
    public List<String> getAdvertencias() { return advertencias; }
}
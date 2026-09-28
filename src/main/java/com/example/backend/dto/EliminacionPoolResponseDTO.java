package com.example.backend.dto;

import java.util.List;

public class EliminacionPoolResponseDTO {

    private String mensaje;
    private List<String> advertencias;

    public EliminacionPoolResponseDTO(String mensaje, List<String> advertencias) {
        this.mensaje = mensaje;
        this.advertencias = advertencias;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
}
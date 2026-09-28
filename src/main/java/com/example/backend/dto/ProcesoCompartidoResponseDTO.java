package com.example.backend.dto;

import java.time.LocalDateTime;

public class ProcesoCompartidoResponseDTO {

    private Long procesoId;
    private String procesoNombre;
    private Long empresaDestinoId;
    private String empresaDestinoNombre;
    private LocalDateTime fechaCompartido;

    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public String getProcesoNombre() { return procesoNombre; }
    public void setProcesoNombre(String procesoNombre) { this.procesoNombre = procesoNombre; }
    public Long getEmpresaDestinoId() { return empresaDestinoId; }
    public void setEmpresaDestinoId(Long empresaDestinoId) { this.empresaDestinoId = empresaDestinoId; }
    public String getEmpresaDestinoNombre() { return empresaDestinoNombre; }
    public void setEmpresaDestinoNombre(String empresaDestinoNombre) { this.empresaDestinoNombre = empresaDestinoNombre; }
    public LocalDateTime getFechaCompartido() { return fechaCompartido; }
    public void setFechaCompartido(LocalDateTime fechaCompartido) { this.fechaCompartido = fechaCompartido; }
}
package com.example.backend.dto;

public class FlujoMensajeResponseDTO {

    private Long id;
    private Long procesoId;
    private Long origenId;
    private Long destinoId;
    private Long poolDestinoId;
    private String etiqueta;
    private java.util.List<String> advertencias = new java.util.ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public Long getOrigenId() { return origenId; }
    public void setOrigenId(Long origenId) { this.origenId = origenId; }
    public Long getDestinoId() { return destinoId; }
    public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }
    public Long getPoolDestinoId() { return poolDestinoId; }
    public void setPoolDestinoId(Long poolDestinoId) { this.poolDestinoId = poolDestinoId; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public java.util.List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(java.util.List<String> advertencias) { this.advertencias = advertencias; }
}
package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public class FlujoMensajeRequestDTO {

    @NotNull(message = "El evento origen (throw) es obligatorio")
    private Long origenId;

    // Para mensajes a otro participante
    private Long destinoId;

    // Para mensajes a pool externo (caja negra)
    private Long poolDestinoId;

    private String etiqueta;

    public Long getOrigenId() { return origenId; }
    public void setOrigenId(Long origenId) { this.origenId = origenId; }
    public Long getDestinoId() { return destinoId; }
    public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }
    public Long getPoolDestinoId() { return poolDestinoId; }
    public void setPoolDestinoId(Long poolDestinoId) { this.poolDestinoId = poolDestinoId; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
}
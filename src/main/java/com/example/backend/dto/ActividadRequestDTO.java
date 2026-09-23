package com.example.backend.dto;

import com.example.backend.entity.TipoActividad;

// Este es el que llega desde el formulario/POST (entrada), separado del de salida
public class ActividadRequestDTO {
    private String nombre;
    private TipoActividad tipo;
    private Long laneId;
    private Double posicionX;
    private Double posicionY;

    public ActividadRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoActividad getTipo() { return tipo; }
    public void setTipo(TipoActividad tipo) { this.tipo = tipo; }
    public Long getLaneId() { return laneId; }
    public void setLaneId(Long laneId) { this.laneId = laneId; }
    public Double getPosicionX() { return posicionX; }
    public void setPosicionX(Double posicionX) { this.posicionX = posicionX; }
    public Double getPosicionY() { return posicionY; }
    public void setPosicionY(Double posicionY) { this.posicionY = posicionY; }
}
package com.example.backend.dto;

import com.example.backend.entity.TipoActividad;

public class ActividadDTO {
    private Long id;
    private String nombre;
    private TipoActividad tipo;
    private Long procesoId;
    private Long laneId;
    private String laneNombre;
    private Double posicionX;
    private Double posicionY;

    public ActividadDTO() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoActividad getTipo() { return tipo; }
    public void setTipo(TipoActividad tipo) { this.tipo = tipo; }
    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public Long getLaneId() { return laneId; }
    public void setLaneId(Long laneId) { this.laneId = laneId; }
    public String getLaneNombre() { return laneNombre; }
    public void setLaneNombre(String laneNombre) { this.laneNombre = laneNombre; }
    public Double getPosicionX() { return posicionX; }
    public void setPosicionX(Double posicionX) { this.posicionX = posicionX; }
    public Double getPosicionY() { return posicionY; }
    public void setPosicionY(Double posicionY) { this.posicionY = posicionY; }
}
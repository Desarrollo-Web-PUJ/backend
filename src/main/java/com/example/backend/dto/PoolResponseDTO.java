package com.example.backend.dto;

import com.example.backend.entity.TipoPool;

public class PoolResponseDTO {

    private Long id;
    private String nombre;
    private TipoPool tipo;
    private Long procesoId;
    private int cantidadLanes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoPool getTipo() { return tipo; }
    public void setTipo(TipoPool tipo) { this.tipo = tipo; }
    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public int getCantidadLanes() { return cantidadLanes; }
    public void setCantidadLanes(int cantidadLanes) { this.cantidadLanes = cantidadLanes; }
}
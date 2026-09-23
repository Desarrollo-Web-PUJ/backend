package com.example.backend.dto;

import com.example.backend.entity.EstadoProceso;

public class ProcesoListItemDTO {

    private Long id;
    private String nombre;
    private String categoria;
    private EstadoProceso estado;
    private Boolean activo;

    public ProcesoListItemDTO(Long id, String nombre, String categoria, EstadoProceso estado, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.estado = estado;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public EstadoProceso getEstado() { return estado; }
    public Boolean getActivo() { return activo; }
}
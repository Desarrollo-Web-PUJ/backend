package com.example.backend.dto;

import com.example.backend.entity.EstadoProceso;

public class ProcesoEditarRequestDTO {

    private String nombre;
    private String descripcion;
    private String categoria;
    private EstadoProceso estado;

    public ProcesoEditarRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
}

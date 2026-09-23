package com.example.backend.dto;

public class ProcesoCrearRequestDTO {

    private String nombre;
    private String descripcion;
    private String categoria;

    public ProcesoCrearRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}

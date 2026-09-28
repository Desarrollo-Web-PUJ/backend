package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RolProcesoRequestDTO {

    @NotBlank(message = "El nombre del rol de proceso es obligatorio")
    @Size(max = 80, message = "El nombre del rol de proceso no puede superar los 80 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción del rol de proceso no puede superar los 500 caracteres")
    private String descripcion;

    public RolProcesoRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}

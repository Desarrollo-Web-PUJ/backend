package com.example.backend.dto;

public class RolProcesoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    public RolProcesoResponseDTO(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}

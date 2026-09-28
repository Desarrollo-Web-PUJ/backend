package com.example.backend.dto;

public class ProcesoUsoDTO {

    private Long id;
    private String nombre;

    public ProcesoUsoDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}

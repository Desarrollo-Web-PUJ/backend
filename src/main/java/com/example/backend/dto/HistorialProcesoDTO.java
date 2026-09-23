package com.example.backend.dto;

import java.time.LocalDateTime;

public class HistorialProcesoDTO {

    private Long id;
    private String descripcion;
    private LocalDateTime fecha;

    public HistorialProcesoDTO(Long id, String descripcion, LocalDateTime fecha) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFecha() { return fecha; }
}

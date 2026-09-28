package com.example.backend.dto;

import java.util.List;

public class RolProcesoListItemDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<ProcesoUsoDTO> procesos;
    private boolean enUso;
    private boolean eliminable;

    public RolProcesoListItemDTO(
            Long id,
            String nombre,
            String descripcion,
            List<ProcesoUsoDTO> procesos,
            boolean enUso,
            boolean eliminable) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.procesos = procesos;
        this.enUso = enUso;
        this.eliminable = eliminable;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<ProcesoUsoDTO> getProcesos() { return procesos; }
    public boolean isEnUso() { return enUso; }
    public boolean isEliminable() { return eliminable; }
}

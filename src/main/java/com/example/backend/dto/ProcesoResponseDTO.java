package com.example.backend.dto;

import com.example.backend.entity.EstadoProceso;

public class ProcesoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private EstadoProceso estado;
    private Boolean activo;
    private Long empresaId;

    public ProcesoResponseDTO(Long id, String nombre, String descripcion, String categoria,
                               EstadoProceso estado, Boolean activo, Long empresaId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = estado;
        this.activo = activo;
        this.empresaId = empresaId;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public EstadoProceso getEstado() { return estado; }
    public Boolean getActivo() { return activo; }
    public Long getEmpresaId() { return empresaId; }
}
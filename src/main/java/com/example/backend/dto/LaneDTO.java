package com.example.backend.dto;

public class LaneDTO {
    private Long id;
    private String nombre;
    private Long procesoId;
    private Long poolId;
    private Long rolProcesoId;
    private Integer orden;

    public LaneDTO() {}
    public LaneDTO(Long id, String nombre) { this.id = id; this.nombre = nombre; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public Long getRolProcesoId() { return rolProcesoId; }
    public void setRolProcesoId(Long rolProcesoId) { this.rolProcesoId = rolProcesoId; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}

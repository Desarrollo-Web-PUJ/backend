package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public class LaneRequestDTO {

    @NotNull(message = "El rol de proceso de la lane es obligatorio")
    private Long rolProcesoId;

    private Integer orden;

    public Long getRolProcesoId() { return rolProcesoId; }
    public void setRolProcesoId(Long rolProcesoId) { this.rolProcesoId = rolProcesoId; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
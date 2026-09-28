package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public class LaneRequestDTO {

    @NotNull(message = "Debe seleccionar el rol de proceso de la lane")
    private Long rolProcesoId;

    public LaneRequestDTO() {}

    public Long getRolProcesoId() { return rolProcesoId; }
    public void setRolProcesoId(Long rolProcesoId) { this.rolProcesoId = rolProcesoId; }
}

package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public class CompartirProcesoRequestDTO {

    @NotNull(message = "Debe indicar la empresa con la que se comparte el proceso")
    private Long empresaDestinoId;

    public Long getEmpresaDestinoId() { return empresaDestinoId; }
    public void setEmpresaDestinoId(Long empresaDestinoId) { this.empresaDestinoId = empresaDestinoId; }
}
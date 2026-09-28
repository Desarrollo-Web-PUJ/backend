package com.example.backend.dto;

import com.example.backend.entity.TipoGateway;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayRequestDTO {

    @NotNull
    private TipoGateway tipo;

    private Double posicionX;

    private Double posicionY;
}
package com.example.backend.dto;

import com.example.backend.entity.TipoGateway;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayResponseDTO {

    private Long id;
    private Long procesoId;
    private Long poolId;
    private TipoGateway tipo;
    private Double posicionX;
    private Double posicionY;
}

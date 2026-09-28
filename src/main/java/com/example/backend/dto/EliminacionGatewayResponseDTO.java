package com.example.backend.dto;

import java.util.List;

public class EliminacionGatewayResponseDTO {

    private Long gatewayId;
    private int arcosEliminados;
    private List<String> advertencias;

    public EliminacionGatewayResponseDTO(
            Long gatewayId,
            int arcosEliminados,
            List<String> advertencias) {

        this.gatewayId = gatewayId;
        this.arcosEliminados = arcosEliminados;
        this.advertencias = advertencias;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public int getArcosEliminados() {
        return arcosEliminados;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }
}
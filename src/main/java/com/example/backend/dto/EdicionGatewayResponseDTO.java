package com.example.backend.dto;

import java.util.List;

public class EdicionGatewayResponseDTO {

    private GatewayResponseDTO gateway;
    private List<String> advertencias;

    public EdicionGatewayResponseDTO(
            GatewayResponseDTO gateway,
            List<String> advertencias) {

        this.gateway = gateway;
        this.advertencias = advertencias;
    }

    public GatewayResponseDTO getGateway() {
        return gateway;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }
}
package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EliminacionArcoResponseDTO {

    private String mensaje;
    private List<String> advertencias;

    public EliminacionArcoResponseDTO(
            String mensaje,
            List<String> advertencias) {

        this.mensaje = mensaje;
        this.advertencias = advertencias;
    }
}

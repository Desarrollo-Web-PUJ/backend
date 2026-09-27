package com.example.backend.dto;

import com.example.backend.entity.TipoNodo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArcoResponseDTO {

    private Long id;
    private Long procesoId;

    private TipoNodo tipoOrigen;
    private Long origenId;

    private TipoNodo tipoDestino;
    private Long destinoId;

    private String etiqueta;
    private String condicion;
}
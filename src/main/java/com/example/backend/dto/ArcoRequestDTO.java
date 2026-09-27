package com.example.backend.dto;

import com.example.backend.entity.TipoNodo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArcoRequestDTO {

    @NotNull
    private TipoNodo tipoOrigen;

    @NotNull
    private Long origenId;

    @NotNull
    private TipoNodo tipoDestino;

    @NotNull
    private Long destinoId;

    private String etiqueta;

    private String condicion;
}
package com.example.backend.dto;

import com.example.backend.entity.TipoPool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PoolRequestDTO {

    @NotBlank(message = "El nombre del pool es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de pool es obligatorio")
    private TipoPool tipo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoPool getTipo() { return tipo; }
    public void setTipo(TipoPool tipo) { this.tipo = tipo; }
}
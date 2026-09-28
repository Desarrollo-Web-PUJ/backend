package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CampoMensajeDTO {

    @NotBlank(message = "El nombre del campo es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de dato del campo es obligatorio")
    private String tipoDato;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoDato() { return tipoDato; }
    public void setTipoDato(String tipoDato) { this.tipoDato = tipoDato; }
}
package com.example.backend.dto;

import com.example.backend.entity.ComportamientoFallo;
import com.example.backend.entity.ComportamientoSinCorrelacion;
import com.example.backend.entity.TipoDestinoExterno;
import com.example.backend.entity.TipoEventoMensaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventoMensajeRequestDTO {

    @NotNull(message = "El tipo de evento es obligatorio")
    private TipoEventoMensaje tipo;

    @NotNull(message = "El pool es obligatorio")
    private Long poolId;

    private Long laneId;

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    private String nombreMensaje;

    // HU-28
    private String claveCorrelacion;
    private ComportamientoSinCorrelacion comportamientoSinCorrelacion;

    // HU-27
    private Boolean origenExterno = false;

    // HU-26
    private TipoDestinoExterno tipoDestinoExterno;
    private ComportamientoFallo comportamientoFallo;

    // HU-25 / HU-27
    private List<CampoMensajeDTO> campos = new ArrayList<>();

    // HU-27
    private List<Long> actividadesConsumidoras = new ArrayList<>();

    private Double posicionX;
    private Double posicionY;

    // getters / setters
    public TipoEventoMensaje getTipo() { return tipo; }
    public void setTipo(TipoEventoMensaje tipo) { this.tipo = tipo; }
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public Long getLaneId() { return laneId; }
    public void setLaneId(Long laneId) { this.laneId = laneId; }
    public String getNombreMensaje() { return nombreMensaje; }
    public void setNombreMensaje(String nombreMensaje) { this.nombreMensaje = nombreMensaje; }
    public String getClaveCorrelacion() { return claveCorrelacion; }
    public void setClaveCorrelacion(String claveCorrelacion) { this.claveCorrelacion = claveCorrelacion; }
    public ComportamientoSinCorrelacion getComportamientoSinCorrelacion() { return comportamientoSinCorrelacion; }
    public void setComportamientoSinCorrelacion(ComportamientoSinCorrelacion c) { this.comportamientoSinCorrelacion = c; }
    public Boolean getOrigenExterno() { return origenExterno; }
    public void setOrigenExterno(Boolean origenExterno) { this.origenExterno = origenExterno; }
    public TipoDestinoExterno getTipoDestinoExterno() { return tipoDestinoExterno; }
    public void setTipoDestinoExterno(TipoDestinoExterno t) { this.tipoDestinoExterno = t; }
    public ComportamientoFallo getComportamientoFallo() { return comportamientoFallo; }
    public void setComportamientoFallo(ComportamientoFallo c) { this.comportamientoFallo = c; }
    public List<CampoMensajeDTO> getCampos() { return campos; }
    public void setCampos(List<CampoMensajeDTO> campos) { this.campos = campos; }
    public List<Long> getActividadesConsumidoras() { return actividadesConsumidoras; }
    public void setActividadesConsumidoras(List<Long> a) { this.actividadesConsumidoras = a; }
    public Double getPosicionX() { return posicionX; }
    public void setPosicionX(Double posicionX) { this.posicionX = posicionX; }
    public Double getPosicionY() { return posicionY; }
    public void setPosicionY(Double posicionY) { this.posicionY = posicionY; }
}
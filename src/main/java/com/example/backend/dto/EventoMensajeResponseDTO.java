package com.example.backend.dto;

import com.example.backend.entity.ComportamientoFallo;
import com.example.backend.entity.ComportamientoSinCorrelacion;
import com.example.backend.entity.TipoDestinoExterno;
import com.example.backend.entity.TipoEventoMensaje;

import java.util.ArrayList;
import java.util.List;

public class EventoMensajeResponseDTO {

    private Long id;
    private Long procesoId;
    private Long poolId;
    private String poolNombre;
    private Long laneId;
    private String laneNombre;
    private TipoEventoMensaje tipo;
    private String nombreMensaje;
    private String claveCorrelacion;
    private Boolean correlacionDefinida;
    private ComportamientoSinCorrelacion comportamientoSinCorrelacion;
    private Boolean origenExterno;
    private TipoDestinoExterno tipoDestinoExterno;
    private ComportamientoFallo comportamientoFallo;
    private List<CampoMensajeDTO> campos = new ArrayList<>();
    private List<Long> actividadesConsumidoras = new ArrayList<>();
    private Double posicionX;
    private Double posicionY;
    private List<String> advertencias = new ArrayList<>();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcesoId() { return procesoId; }
    public void setProcesoId(Long procesoId) { this.procesoId = procesoId; }
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public String getPoolNombre() { return poolNombre; }
    public void setPoolNombre(String poolNombre) { this.poolNombre = poolNombre; }
    public Long getLaneId() { return laneId; }
    public void setLaneId(Long laneId) { this.laneId = laneId; }
    public String getLaneNombre() { return laneNombre; }
    public void setLaneNombre(String laneNombre) { this.laneNombre = laneNombre; }
    public TipoEventoMensaje getTipo() { return tipo; }
    public void setTipo(TipoEventoMensaje tipo) { this.tipo = tipo; }
    public String getNombreMensaje() { return nombreMensaje; }
    public void setNombreMensaje(String nombreMensaje) { this.nombreMensaje = nombreMensaje; }
    public String getClaveCorrelacion() { return claveCorrelacion; }
    public void setClaveCorrelacion(String claveCorrelacion) { this.claveCorrelacion = claveCorrelacion; }
    public Boolean getCorrelacionDefinida() { return correlacionDefinida; }
    public void setCorrelacionDefinida(Boolean correlacionDefinida) { this.correlacionDefinida = correlacionDefinida; }
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
    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
}
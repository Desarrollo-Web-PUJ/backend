package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "eventos_mensaje")
@Getter
@Setter
public class EventoMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id")
    private Lane lane;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoMensaje tipo;

    @Column(name = "nombre_mensaje", nullable = false)
    private String nombreMensaje;

    // HU-28: clave de correlación
    @Column(name = "clave_correlacion")
    private String claveCorrelacion;

    @Column(name = "correlacion_definida", nullable = false)
    private Boolean correlacionDefinida = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "comportamiento_sin_correlacion")
    private ComportamientoSinCorrelacion comportamientoSinCorrelacion;

    // HU-27: catch sin throw emparejado (mensaje externo)
    @Column(name = "origen_externo", nullable = false)
    private Boolean origenExterno = false;

    // HU-26: solo aplica a THROW hacia pool externo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destino_externo")
    private TipoDestinoExterno tipoDestinoExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "comportamiento_fallo")
    private ComportamientoFallo comportamientoFallo;

    // HU-25 / HU-27: campos documentados como JSON string
    @Column(name = "campos_mensaje", columnDefinition = "TEXT")
    private String camposMensaje;

    // HU-27: qué actividades usan los datos recibidos (JSON de ids)
    @Column(name = "actividades_consumidoras", columnDefinition = "TEXT")
    private String actividadesConsumidoras;

    @Column(name = "posicion_x")
    private Double posicionX;

    @Column(name = "posicion_y")
    private Double posicionY;

    @Column(nullable = false)
    private Boolean activo = true;
}
package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flujos_mensaje")
@Getter
@Setter
public class FlujoMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    // HU-25: throw de origen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_id", nullable = false)
    private EventoMensaje origen;

    // HU-25/27: catch destino (mensaje a otro participante)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id")
    private EventoMensaje destino;

    // HU-26: pool externo destino (caja negra)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_destino_id")
    private Pool poolDestino;

    @Column
    private String etiqueta;

    @Column(nullable = false)
    private Boolean activo = true;
}
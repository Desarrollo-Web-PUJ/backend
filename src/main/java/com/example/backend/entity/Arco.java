package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "arcos")
@Getter
@Setter
public class Arco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origen", nullable = false)
    private TipoNodo tipoOrigen;

    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destino", nullable = false)
    private TipoNodo tipoDestino;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column
    private String etiqueta;

    @Column
    private String condicion;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo = true;
}

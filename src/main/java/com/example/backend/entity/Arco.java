package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "arcos",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "proceso_id",
                "tipo_origen",
                "origen_id",
                "tipo_destino",
                "destino_id"
            }
        )
    }
)
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
}

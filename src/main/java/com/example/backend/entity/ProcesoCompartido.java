package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "procesos_compartidos",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"proceso_id", "empresa_destino_id"})
    }
)
@Getter
@Setter
public class ProcesoCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_destino_id", nullable = false)
    private Empresa empresaDestino;

    @Column(nullable = false)
    private LocalDateTime fechaCompartido;

    @Column(nullable = false)
    private Boolean activo = true;
}
package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lanes")
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre propio de la lane. Solo se usa cuando la lane no referencia un rol
    // (lanes creadas antes de existir el catalogo de roles).
    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    // Rol de proceso que da nombre a la lane (nullable para lanes anteriores)
    @ManyToOne
    @JoinColumn(name = "rol_proceso_id")
    private RolProceso rolProceso;

    // NUEVO (HU-22): la lane vive dentro de un pool. Nullable para no romper
    // filas ya existentes creadas antes de este cambio.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id")
    private Pool pool;

    // NUEVO (HU-22): posición de la lane dentro del pool, para reordenar.
    @Column(nullable = false)
    private Integer orden = 0;

    // NUEVO (HU-22): borrado lógico, igual patrón que RolProceso/Actividad.
    @Column(nullable = false)
    private Boolean activo = true;

    public Lane() {}

    public Lane(String nombre, Proceso proceso) {
        this.nombre = nombre;
        this.proceso = proceso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Si la lane referencia un rol, el nombre es siempre el actual del rol:
    // renombrar el rol renombra todas las lanes que lo usan.
    public String getNombre() {
        return rolProceso != null ? rolProceso.getNombre() : nombre;
    }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public Proceso getProceso() { return proceso; }
    public void setProceso(Proceso proceso) { this.proceso = proceso; }
    public RolProceso getRolProceso() { return rolProceso; }
    public void setRolProceso(RolProceso rolProceso) { this.rolProceso = rolProceso; }
    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
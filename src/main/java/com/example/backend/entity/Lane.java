package com.example.backend.entity;

import jakarta.persistence.*;

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
}

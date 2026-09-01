package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@Getter
@Setter
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(name = "correo_contacto", nullable = false)
    private String correoContacto;

    @OneToMany(mappedBy = "empresa")
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "empresa")
    private List<Proceso> procesos = new ArrayList<>();
}
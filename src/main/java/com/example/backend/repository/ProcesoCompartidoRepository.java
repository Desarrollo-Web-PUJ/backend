package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.ProcesoCompartido;

public interface ProcesoCompartidoRepository extends JpaRepository<ProcesoCompartido, Long> {

    List<ProcesoCompartido> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<ProcesoCompartido> findByProcesoIdAndEmpresaDestinoIdAndActivoTrue(Long procesoId, Long empresaDestinoId);

    List<ProcesoCompartido> findByEmpresaDestinoIdAndActivoTrue(Long empresaDestinoId);

    boolean existsByProcesoIdAndEmpresaDestinoIdAndActivoTrue(Long procesoId, Long empresaDestinoId);
}
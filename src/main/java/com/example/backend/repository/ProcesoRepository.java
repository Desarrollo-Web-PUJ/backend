package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.entity.EstadoProceso;
import com.example.backend.entity.Proceso;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);

    boolean existsByNombreAndEmpresaIdAndIdNot(String nombre, Long empresaId, Long id);

    Optional<Proceso> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId " +
           "AND (:incluirInactivos = true OR p.activo = true) " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "AND (:categoria IS NULL OR p.categoria = :categoria)")
    Page<Proceso> buscar(@Param("empresaId") Long empresaId,
                          @Param("incluirInactivos") boolean incluirInactivos,
                          @Param("nombre") String nombre,
                          @Param("estado") EstadoProceso estado,
                          @Param("categoria") String categoria,
                          Pageable pageable);
}
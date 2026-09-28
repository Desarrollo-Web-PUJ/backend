package com.example.backend.repository;

import com.example.backend.entity.RolProceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {

    Optional<RolProceso> findByIdAndEmpresaIdAndActivoTrue(
            Long id,
            Long empresaId
    );

    boolean existsByEmpresaIdAndNombreIgnoreCaseAndActivoTrue(
            Long empresaId,
            String nombre
    );

    boolean existsByEmpresaIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(
            Long empresaId,
            String nombre,
            Long id
    );

    @Query("""
            SELECT r
            FROM RolProceso r
            WHERE r.empresa.id = :empresaId
            AND r.activo = true
            AND LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
            """)
    Page<RolProceso> buscar(
            @Param("empresaId") Long empresaId,
            @Param("nombre") String nombre,
            Pageable pageable
    );

    List<RolProceso> findByEmpresaIdAndActivoTrueOrderByNombreAsc(Long empresaId);
}

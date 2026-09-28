package com.example.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.entity.Lane;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findByProcesoIdAndActivoTrueOrderByOrdenAsc(Long procesoId);

    // Devuelve filas [rolId, procesoId, procesoNombre] de los procesos
    // en los que cada rol esta siendo usado por alguna lane activa.
    @Query("""
            SELECT DISTINCT l.rolProceso.id, p.id, p.nombre
            FROM Lane l
            JOIN l.proceso p
            WHERE l.rolProceso.id IN :rolIds
            AND l.activo = true
            ORDER BY p.nombre
            """)
    List<Object[]> findUsoDeRoles(@Param("rolIds") Collection<Long> rolIds);

    // NUEVO (HU-22): lanes de un pool, ordenadas
    List<Lane> findByPoolIdAndActivoTrueOrderByOrdenAsc(Long poolId);

    Optional<Lane> findByIdAndPoolId(Long id, Long poolId);

    boolean existsByPoolIdAndRolProcesoIdAndActivoTrue(Long poolId, Long rolProcesoId);

    // Uso de un rol de proceso en cualquier lane activa.
    boolean existsByRolProcesoIdAndActivoTrue(Long rolProcesoId);
}

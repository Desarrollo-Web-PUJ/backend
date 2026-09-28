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

    boolean existsByProcesoIdAndRolProcesoId(Long procesoId, Long rolProcesoId);

    // Devuelve filas [rolId, procesoId, procesoNombre] de los procesos activos
    // en los que cada rol esta siendo usado por alguna lane
    @Query("""
            SELECT DISTINCT l.rolProceso.id, p.id, p.nombre
            FROM Lane l
            JOIN l.proceso p
            WHERE l.rolProceso.id IN :rolIds
            AND l.activo = true
            AND p.activo = true
            ORDER BY p.nombre
            """)
    List<Object[]> findUsoDeRoles(@Param("rolIds") Collection<Long> rolIds);

    // NUEVO (HU-22): lanes de un pool, ordenadas
    List<Lane> findByPoolIdAndActivoTrueOrderByOrdenAsc(Long poolId);

    Optional<Lane> findByIdAndPoolId(Long id, Long poolId);

    boolean existsByPoolIdAndRolProcesoId(Long poolId, Long rolProcesoId);

    // NUEVO (HU-24): uso de un rol de proceso en CUALQUIER lane de la empresa
    // (no solo en un proceso puntual). Úsalo en RolProcesoServiceImpl.eliminar()
    // en lugar de / además de la validación actual — coordinar con el dueño de Roles.
    boolean existsByRolProcesoIdAndActivoTrue(Long rolProcesoId);
}

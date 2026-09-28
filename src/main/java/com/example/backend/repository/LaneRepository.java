package com.example.backend.repository;

import com.example.backend.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findByProcesoId(Long procesoId);

    boolean existsByProcesoIdAndRolProcesoId(Long procesoId, Long rolProcesoId);

    // Devuelve filas [rolId, procesoId, procesoNombre] de los procesos activos
    // en los que cada rol esta siendo usado por alguna lane
    @Query("""
            SELECT DISTINCT l.rolProceso.id, p.id, p.nombre
            FROM Lane l
            JOIN l.proceso p
            WHERE l.rolProceso.id IN :rolIds
            AND p.activo = true
            ORDER BY p.nombre
            """)
    List<Object[]> findUsoDeRoles(@Param("rolIds") Collection<Long> rolIds);
}

package com.example.backend.repository;

import com.example.backend.entity.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    List<Gateway> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<Gateway> findByIdAndProcesoIdAndActivoTrue(
            Long id,
            Long procesoId
    );

    boolean existsByPoolIdAndActivoTrue(Long poolId);
}

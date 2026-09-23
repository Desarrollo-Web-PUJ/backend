package com.example.backend.repository;

import com.example.backend.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LaneRepository extends JpaRepository<Lane, Long> {
    List<Lane> findByProcesoId(Long procesoId);
}
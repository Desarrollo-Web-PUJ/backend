package com.example.backend.controller;

import com.example.backend.dto.LaneDTO;
import com.example.backend.dto.LaneRequestDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.RolProcesoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procesos/{procesoId}/lanes")
public class LaneController {

    private final RolProcesoService rolProcesoService;

    public LaneController(RolProcesoService rolProcesoService) {
        this.rolProcesoService = rolProcesoService;
    }

    // Nombra una lane del proceso con un rol de proceso de la empresa
    @PostMapping
    public ResponseEntity<LaneDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody LaneRequestDTO request,
            HttpSession session) {

        Long empresaId = (Long) session.getAttribute("empresaId");
        RolUsuario rol = (RolUsuario) session.getAttribute("rol");

        if (empresaId == null || rol == null) {
            throw new SesionNoAutenticadaException(
                    "No hay una sesión iniciada"
            );
        }

        LaneDTO lane = rolProcesoService.crearLaneConRol(
                procesoId,
                request.getRolProcesoId(),
                empresaId,
                rol
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lane);
    }
}

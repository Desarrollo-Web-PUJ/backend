package com.example.backend.controller;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.dto.EliminacionActividadResponseDTO;
import com.example.backend.dto.LaneDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.ActividadService;
import com.example.backend.service.LaneService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final LaneService laneService;

    public ActividadController(
            ActividadService actividadService,
            LaneService laneService) {
        this.actividadService = actividadService;
        this.laneService = laneService;
    }

    @GetMapping
    public ResponseEntity<List<ActividadDTO>> listar(
            @PathVariable Long procesoId) {

        List<ActividadDTO> actividades =
                actividadService.listarPorProceso(procesoId);

        return ResponseEntity.ok(actividades);
    }

    @PostMapping
    public ResponseEntity<ActividadDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody ActividadRequestDTO request) {

        ActividadDTO actividad =
                actividadService.crearActividad(procesoId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(actividad);
    }

    @PutMapping("/{actividadId}")
    public ResponseEntity<ActividadDTO> editar(
            @PathVariable Long procesoId,
            @PathVariable Long actividadId,
            @Valid @RequestBody ActividadRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        ActividadDTO actividad =
                actividadService.editarActividad(
                        procesoId,
                        actividadId,
                        request,
                        empresaId,
                        rol
                );

        return ResponseEntity.ok(actividad);
    }

    @DeleteMapping("/{actividadId}")
    public ResponseEntity<EliminacionActividadResponseDTO> eliminar(
            @PathVariable Long procesoId,
            @PathVariable Long actividadId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {

        if (!confirmar) {
            throw new OperacionInvalidaException(
                    "Debe confirmar la eliminación de la actividad"
            );
        }

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        EliminacionActividadResponseDTO response =
                actividadService.eliminarActividad(
                        procesoId,
                        actividadId,
                        empresaId,
                        rol
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lanes")
    public ResponseEntity<List<LaneDTO>> listarLanes(
            @PathVariable Long procesoId) {

        List<LaneDTO> lanes =
                laneService.listarPorProceso(procesoId);

        return ResponseEntity.ok(lanes);
    }

    @PostMapping("/lanes")
    public ResponseEntity<LaneDTO> crearLane(
            @PathVariable Long procesoId,
            @RequestParam String nombreLane) {

        LaneDTO lane =
                laneService.crearLane(procesoId, nombreLane);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lane);
    }

    private Long obtenerEmpresaId(HttpSession session) {

        Long empresaId =
                (Long) session.getAttribute("empresaId");

        if (empresaId == null) {
            throw new SesionNoAutenticadaException(
                    "No hay una empresa asociada a la sesión"
            );
        }

        return empresaId;
    }

    private RolUsuario obtenerRol(HttpSession session) {

        RolUsuario rol =
                (RolUsuario) session.getAttribute("rol");

        if (rol == null) {
            throw new SesionNoAutenticadaException(
                    "No hay un rol asociado a la sesión"
            );
        }

        return rol;
    }
}
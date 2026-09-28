package com.example.backend.controller;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.dto.EliminacionActividadResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.ActividadService;
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

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    public ResponseEntity<List<ActividadDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        List<ActividadDTO> actividades =
                actividadService.listarPorProceso(procesoId, empresaId);

        return ResponseEntity.ok(actividades);
    }

    @PostMapping
    public ResponseEntity<ActividadDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody ActividadRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        ActividadDTO actividad =
                actividadService.crearActividad(procesoId, request, empresaId, rol);

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

package com.example.backend.controller;

import com.example.backend.dto.EliminacionRolProcesoResponseDTO;
import com.example.backend.dto.RolProcesoListItemDTO;
import com.example.backend.dto.RolProcesoRequestDTO;
import com.example.backend.dto.RolProcesoResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.RolProcesoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles-proceso")
public class RolProcesoController {

    private final RolProcesoService rolProcesoService;

    public RolProcesoController(RolProcesoService rolProcesoService) {
        this.rolProcesoService = rolProcesoService;
    }

    // HU-20: Consultar roles de proceso
    @GetMapping
    public ResponseEntity<Page<RolProcesoListItemDTO>> consultar(
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        return ResponseEntity.ok(
                rolProcesoService.consultar(nombre, pageable, empresaId)
        );
    }

    // HU-17: Crear rol de proceso
    @PostMapping
    public ResponseEntity<RolProcesoResponseDTO> crear(
            @Valid @RequestBody RolProcesoRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        RolProcesoResponseDTO response =
                rolProcesoService.crear(request, empresaId, rol);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // HU-18: Editar rol de proceso
    @PutMapping("/{rolProcesoId}")
    public ResponseEntity<RolProcesoResponseDTO> editar(
            @PathVariable Long rolProcesoId,
            @Valid @RequestBody RolProcesoRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        return ResponseEntity.ok(
                rolProcesoService.editar(rolProcesoId, request, empresaId, rol)
        );
    }

    // HU-19: Eliminar rol de proceso
    @DeleteMapping("/{rolProcesoId}")
    public ResponseEntity<EliminacionRolProcesoResponseDTO> eliminar(
            @PathVariable Long rolProcesoId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {

        if (!confirmar) {
            throw new OperacionInvalidaException(
                    "Debe confirmar la eliminación del rol de proceso"
            );
        }

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        return ResponseEntity.ok(
                rolProcesoService.eliminar(rolProcesoId, empresaId, rol)
        );
    }

    private Long obtenerEmpresaId(HttpSession session) {

        Long empresaId = (Long) session.getAttribute("empresaId");

        if (empresaId == null) {
            throw new SesionNoAutenticadaException(
                    "No hay una empresa asociada a la sesión"
            );
        }

        return empresaId;
    }

    private RolUsuario obtenerRol(HttpSession session) {

        RolUsuario rol = (RolUsuario) session.getAttribute("rol");

        if (rol == null) {
            throw new SesionNoAutenticadaException(
                    "No hay un rol asociado a la sesión"
            );
        }

        return rol;
    }
}

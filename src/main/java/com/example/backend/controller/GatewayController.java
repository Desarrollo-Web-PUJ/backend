package com.example.backend.controller;

import com.example.backend.dto.EdicionGatewayResponseDTO;
import com.example.backend.dto.EliminacionGatewayResponseDTO;
import com.example.backend.dto.GatewayRequestDTO;
import com.example.backend.dto.GatewayResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.GatewayService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(
            GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<List<GatewayResponseDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        List<GatewayResponseDTO> gateways =
                gatewayService.listarPorProceso(
                        procesoId,
                        empresaId
                );

        return ResponseEntity.ok(gateways);
    }

    @PostMapping
    public ResponseEntity<GatewayResponseDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody GatewayRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        GatewayResponseDTO gateway =
                gatewayService.crearGateway(
                        procesoId,
                        request,
                        empresaId,
                        rol
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gateway);
    }

    @PutMapping("/{gatewayId}")
    public ResponseEntity<EdicionGatewayResponseDTO> editar(
            @PathVariable Long procesoId,
            @PathVariable Long gatewayId,
            @Valid @RequestBody GatewayRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        EdicionGatewayResponseDTO response =
                gatewayService.editarGateway(
                        procesoId,
                        gatewayId,
                        request,
                        empresaId,
                        rol
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{gatewayId}")
    public ResponseEntity<EliminacionGatewayResponseDTO> eliminar(
            @PathVariable Long procesoId,
            @PathVariable Long gatewayId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        EliminacionGatewayResponseDTO response =
                gatewayService.eliminarGateway(
                        procesoId,
                        gatewayId,
                        empresaId,
                        rol,
                        confirmar
                );

        return ResponseEntity.ok(response);
    }

    private Long obtenerEmpresaId(
            HttpSession session) {

        Long empresaId =
                (Long) session.getAttribute("empresaId");

        if (empresaId == null) {
            throw new SesionNoAutenticadaException(
                    "No hay una empresa asociada a la sesión"
            );
        }

        return empresaId;
    }

    private RolUsuario obtenerRol(
            HttpSession session) {

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
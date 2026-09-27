package com.example.backend.controller;

import com.example.backend.dto.ProcesoCrearRequestDTO;
import com.example.backend.dto.ProcesoEditarRequestDTO;
import com.example.backend.entity.EstadoProceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.service.ProcesoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    private Long usuarioIdSesion(HttpSession session) {
        return (Long) session.getAttribute("usuarioId");
    }

    private Long empresaIdSesion(HttpSession session) {
        return (Long) session.getAttribute("empresaId");
    }

    private RolUsuario rolSesion(HttpSession session) {
        return (RolUsuario) session.getAttribute("rol");
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String nombre,
                          @RequestParam(required = false) EstadoProceso estado,
                          @RequestParam(required = false) String categoria,
                          @RequestParam(defaultValue = "false") boolean incluirInactivos,
                          @PageableDefault(size = 10) Pageable pageable,
                          HttpSession session,
                          Model model) {
        Long empresaId = empresaIdSesion(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("procesos", procesoService.listarProcesos(
                empresaId, nombre, estado, categoria, incluirInactivos, pageable));
        model.addAttribute("nombre", nombre);
        model.addAttribute("estado", estado);
        model.addAttribute("categoria", categoria);
        model.addAttribute("incluirInactivos", incluirInactivos);
        model.addAttribute("estados", EstadoProceso.values());
        return "procesos/list";
    }

    @GetMapping("/nuevo")
    public String formularioCrear(Model model) {
        model.addAttribute("proceso", new ProcesoCrearRequestDTO());
        return "procesos/form";
    }

    @PostMapping
    public String crear(@ModelAttribute("proceso") ProcesoCrearRequestDTO request,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long empresaId = empresaIdSesion(session);
        Long usuarioId = usuarioIdSesion(session);
        if (empresaId == null || usuarioId == null) return "redirect:/login";

        try {
            var creado = procesoService.crearProceso(empresaId, usuarioId, request);
            redirectAttributes.addFlashAttribute("mensaje", "Proceso creado correctamente");
            return "redirect:/procesos/" + creado.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "procesos/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, HttpSession session, Model model) {
        Long empresaId = empresaIdSesion(session);
        if (empresaId == null) return "redirect:/login";

        try {
            model.addAttribute("proceso", procesoService.obtenerDetalle(id, empresaId));
            return "procesos/detalle";
        } catch (IllegalArgumentException e) {
            return "redirect:/procesos";
        }
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, HttpSession session, Model model) {
        Long empresaId = empresaIdSesion(session);
        if (empresaId == null) return "redirect:/login";

        var detalle = procesoService.obtenerDetalle(id, empresaId);

        ProcesoEditarRequestDTO request = new ProcesoEditarRequestDTO();
        request.setNombre(detalle.getNombre());
        request.setDescripcion(detalle.getDescripcion());
        request.setCategoria(detalle.getCategoria());
        request.setEstado(detalle.getEstado());

        model.addAttribute("proceso", request);
        model.addAttribute("procesoId", id);
        model.addAttribute("estados", EstadoProceso.values());
        return "procesos/form-editar";
    }

    @PostMapping("/{id}")
    public String editar(@PathVariable Long id,
                          @ModelAttribute("proceso") ProcesoEditarRequestDTO request,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        Long empresaId = empresaIdSesion(session);
        Long usuarioId = usuarioIdSesion(session);
        RolUsuario rol = rolSesion(session);
        if (empresaId == null || usuarioId == null || rol == null) return "redirect:/login";

        try {
            procesoService.editarProceso(id, empresaId, usuarioId, rol, request);
            redirectAttributes.addFlashAttribute("mensaje", "Proceso actualizado correctamente");
            return "redirect:/procesos/" + id;
        } catch (PermisoDenegadoException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("procesoId", id);
            model.addAttribute("estados", EstadoProceso.values());
            return "procesos/form-editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long empresaId = empresaIdSesion(session);
        Long usuarioId = usuarioIdSesion(session);
        RolUsuario rol = rolSesion(session);
        if (empresaId == null || usuarioId == null || rol == null) return "redirect:/login";

        try {
            procesoService.eliminarProceso(id, empresaId, usuarioId, rol);
            redirectAttributes.addFlashAttribute("mensaje", "Proceso eliminado correctamente");
        } catch (PermisoDenegadoException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/procesos";
    }

    @GetMapping("/{id}/historial")
    public String historial(@PathVariable Long id, HttpSession session, Model model) {
        Long empresaId = empresaIdSesion(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("historial", procesoService.obtenerHistorial(id, empresaId));
        model.addAttribute("procesoId", id);
        return "procesos/historial";
    }
}

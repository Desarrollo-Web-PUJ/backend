package com.example.backend.controller;

import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.entity.TipoActividad;
import com.example.backend.service.ActividadService;
import com.example.backend.service.LaneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final LaneService laneService;

    public ActividadController(ActividadService actividadService, LaneService laneService) {
        this.actividadService = actividadService;
        this.laneService = laneService;
    }

    @GetMapping
    public String listar(@PathVariable Long procesoId, Model model) {
        model.addAttribute("procesoId", procesoId);
        model.addAttribute("actividades", actividadService.listarPorProceso(procesoId));
        return "actividades/list";
    }

    @GetMapping("/nueva")
    public String formularioNuevo(@PathVariable Long procesoId, Model model) {
        model.addAttribute("procesoId", procesoId);
        model.addAttribute("actividad", new ActividadRequestDTO());
        model.addAttribute("lanes", laneService.listarPorProceso(procesoId));
        model.addAttribute("tipos", TipoActividad.values());
        return "actividades/form";
    }

    @PostMapping
    public String crear(@PathVariable Long procesoId,
                         @ModelAttribute("actividad") ActividadRequestDTO request,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            actividadService.crearActividad(procesoId, request);
            redirectAttributes.addFlashAttribute("mensaje", "Actividad creada correctamente");
            return "redirect:/procesos/" + procesoId + "/actividades";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("procesoId", procesoId);
            model.addAttribute("lanes", laneService.listarPorProceso(procesoId));
            model.addAttribute("tipos", TipoActividad.values());
            return "actividades/form";
        }
    }

    // endpoint rápido para poder crear una lane desde el mismo formulario
    @PostMapping("/lanes")
    public String crearLane(@PathVariable Long procesoId, @RequestParam String nombreLane) {
        laneService.crearLane(procesoId, nombreLane);
        return "redirect:/procesos/" + procesoId + "/actividades/nueva";
    }
}
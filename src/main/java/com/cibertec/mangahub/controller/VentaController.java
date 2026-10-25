package com.cibertec.mangahub.controller;

import com.cibertec.mangahub.dto.VentaDTO;
import com.cibertec.mangahub.repository.*;
import com.cibertec.mangahub.service.VentaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;
    private final ClienteRepository clienteRepository;
    private final MangaRepository mangaRepository;
    private boolean autenticado(HttpSession session) { return session.getAttribute("idUsuario") instanceof Integer; }
    private void catalogos(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).toList());
        model.addAttribute("mangas", mangaRepository.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getActivo()) && m.getStock() != null && m.getStock() > 0).toList());
    }
    @GetMapping
    public String listar(HttpSession session, Model model) {
        if (!autenticado(session)) return "redirect:/login";
        model.addAttribute("ventas", ventaService.listar()); return "ventas/listado";
    }
    @GetMapping("/nueva")
    public String nueva(HttpSession session, Model model) {
        if (!autenticado(session)) return "redirect:/login";
        model.addAttribute("venta", new VentaDTO()); catalogos(model); return "ventas/nueva";
    }
    @PostMapping
    public String registrar(@ModelAttribute("venta") VentaDTO dto, HttpSession session, Model model) {
        if (!autenticado(session)) return "redirect:/login";
        try {
            var venta = ventaService.registrar(dto, (Integer) session.getAttribute("idUsuario"));
            return "redirect:/ventas/" + venta.getIdVenta();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage()); catalogos(model); return "ventas/nueva";
        }
    }
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (!autenticado(session)) return "redirect:/login";
        try {
            ventaService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Boleta eliminada. Las cantidades se devolvieron al stock.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }

    @GetMapping("/{id}")
    public String boleta(@PathVariable Integer id, HttpSession session, Model model) {
        if (!autenticado(session)) return "redirect:/login";
        try { model.addAttribute("boleta", ventaService.buscar(id)); return "ventas/boleta"; }
        catch (IllegalArgumentException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage()); }
    }
}

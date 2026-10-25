
package com.cibertec.mangahub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cibertec.mangahub.dto.AutenticacionFilter;
import com.cibertec.mangahub.model.Usuario;
import com.cibertec.mangahub.service.AutenticacionService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

	private final AutenticacionService autenticacionService;

	@GetMapping("/login")
	public String login(Model model) {

		model.addAttribute("login", new AutenticacionFilter(null, null));

		return "login";
	}

	@PostMapping("/login")
	public String validarLogin(@ModelAttribute("login") AutenticacionFilter filter, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		Usuario usuario = autenticacionService.authenticate(filter);

		if (usuario != null) {

			session.setAttribute("usuario", usuario.getNombres());
            session.setAttribute("idUsuario", usuario.getIdUsuario());
			session.setAttribute("rol", usuario.getRol());

			redirectAttributes.addFlashAttribute("bienvenida", usuario.getNombres());

			return "redirect:/";
		}

		model.addAttribute("error", "Usuario o contraseña incorrectos");

		return "login";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {

		session.invalidate();

		return "redirect:/login";
	}
}

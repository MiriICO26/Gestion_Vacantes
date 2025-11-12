package com.miriamoc.gestion_vacante.controllers;

import com.miriamoc.gestion_vacante.models.*;
import com.miriamoc.gestion_vacante.repositories.AspiranteRepository;
import com.miriamoc.gestion_vacante.repositories.EmpleadorRepository;
import com.miriamoc.gestion_vacante.services.AuthService;
import com.miriamoc.gestion_vacante.services.VacanteServiceI;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AspiranteRepository aspiranteRepository;
    private final EmpleadorRepository empleadorRepository;
    private final VacanteServiceI vacanteService;

    public AuthController(AuthService authService,
                          AspiranteRepository aspiranteRepository,
                          EmpleadorRepository empleadorRepository,
                          VacanteServiceI vacanteService) {
        this.authService = authService;
        this.aspiranteRepository = aspiranteRepository;
        this.empleadorRepository = empleadorRepository;
        this.vacanteService = vacanteService;
    }

    // ================= LOGIN =================
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("correo", "");
        model.addAttribute("password", "");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String correo,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Usuario usuario = authService.autenticar(correo, password);

        if (usuario == null) {
            model.addAttribute("error", "Credenciales incorrectas");
            return "auth/login";
        }

        if (!usuario.isActive()) {
            model.addAttribute("error", "El usuario está inactivo");
            return "auth/login";
        }

        session.setAttribute("usuario", usuario);

        // Redirigir según rol
        if (usuario.getRol() == Role.ASPIRANTE) {
            return "redirect:/auth/bienvenida/aspirante";
        } else if (usuario.getRol() == Role.EMPLEADOR) {
            return "redirect:/auth/bienvenida/empleador";
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // ================= REGISTRO ASPIRANTE =================
    @GetMapping("/registro-aspirante")
    public String mostrarRegistroAspirante(Model model) {
        model.addAttribute("aspirante", new Aspirante());
        return "auth/registro-aspirante";
    }

    @PostMapping("/registro/aspirante")
    public String registrarAspirante(@Valid @ModelAttribute Aspirante aspirante,
                                     BindingResult result,
                                     Model model) {

        if (result.hasErrors()) return "auth/registro-aspirante";

        // REEMPLAZAMOS usuarioService.existeCorreo() con verificación directa
        if (existeCorreo(aspirante.getCorreo())) {
            model.addAttribute("error", "El correo ya está registrado");
            return "auth/registro-aspirante";
        }

        aspirante.setPassword(authService.hashPassword(aspirante.getPassword()));
        aspirante.setRol(Role.ASPIRANTE);
        aspiranteRepository.save(aspirante);

        return "redirect:/auth/login?registroExitoso=true";
    }

    // ================= REGISTRO EMPLEADOR =================
    @GetMapping("/registro-empleador")
    public String mostrarRegistroEmpleador(Model model) {
        model.addAttribute("empleador", new Empleador());
        return "auth/registro-empleador";
    }

    @PostMapping("/registro/empleador")
    public String registrarEmpleador(@Valid @ModelAttribute Empleador empleador,
                                     BindingResult result,
                                     Model model) {

        if (result.hasErrors()) return "auth/registro-empleador";

        // REEMPLAZAMOS usuarioService.existeCorreo() con verificación directa
        if (existeCorreo(empleador.getCorreo())) {
            model.addAttribute("error", "El correo ya está registrado");
            return "auth/registro-empleador";
        }

        empleador.setPassword(authService.hashPassword(empleador.getPassword()));
        empleador.setRol(Role.EMPLEADOR);
        empleadorRepository.save(empleador);

        return "redirect:/auth/login?registroExitoso=true";
    }

    // ================= BIENVENIDA ASPIRANTE =================
    @GetMapping("/bienvenida/aspirante")
    public String bienvenidaAspirante(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || usuario.getRol() != Role.ASPIRANTE) {
            return "redirect:/auth/login";
        }
        model.addAttribute("usuario", usuario);
        return "aspirante/bienvenida";
    }

    // ================= BIENVENIDA EMPLEADOR =================
    @GetMapping("/bienvenida/empleador")
    public String bienvenidaEmpleador(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || usuario.getRol() != Role.EMPLEADOR) {
            return "redirect:/auth/login";
        }

        Empleador empleador = (Empleador) usuario;

        // Obtener las últimas 3 vacantes del empleador
        List<Vacante> vacantesRecientes = vacanteService.obtenerPorEmpleador(empleador)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        model.addAttribute("usuario", empleador);
        model.addAttribute("vacantesRecientes", vacantesRecientes);
        return "empleador/bienvenida";
    }

    // ================= REDIRECCIÓN AL DASHBOARD =================
    @GetMapping("/empleador/bienvenida")
    public String redirectToBienvenida(HttpSession session) {
        return "redirect:/auth/bienvenida/empleador";
    }

    @GetMapping("/aspirante/mi-perfil")
    public String verMiPerfil(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/auth/login";
        }

        // Obtener datos COMPLETOS del aspirante
        Aspirante aspirante = aspiranteRepository.findByCorreo(usuario.getCorreo())
                .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));

        model.addAttribute("aspirante", aspirante);
        return "aspirante/mi-perfil";
    }

    // ================= METODO PRIVADO PARA VERIFICAR CORREO =================
    private boolean existeCorreo(String correo) {
        // Verificar en ambas tablas
        return aspiranteRepository.findByCorreo(correo).isPresent() ||
                empleadorRepository.findByCorreo(correo).isPresent();
    }
}
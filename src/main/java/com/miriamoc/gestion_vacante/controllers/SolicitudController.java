package com.miriamoc.gestion_vacante.controllers;

import com.miriamoc.gestion_vacante.models.*;
import com.miriamoc.gestion_vacante.repositories.SolicitudRepository;
import com.miriamoc.gestion_vacante.services.EmailTemplateService;
import com.miriamoc.gestion_vacante.services.NotificacionService;
import com.miriamoc.gestion_vacante.services.SolicitudService;
import com.miriamoc.gestion_vacante.services.VacanteServiceI;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final VacanteServiceI vacanteService;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private NotificacionService notificacionService;

    public SolicitudController(SolicitudService solicitudService, VacanteServiceI vacanteService) {
        this.solicitudService = solicitudService;
        this.vacanteService = vacanteService;
    }

    // ================= ASPIRANTE: VER VACANTES DISPONIBLES =================
    @GetMapping("/aspirante/vacantes-disponibles")
    public String verVacantesDisponibles(HttpSession session, Model model) {
        Aspirante aspirante = obtenerAspiranteDeSesion(session);
        if (aspirante == null) {
            return "redirect:/auth/login";
        }

        List<Vacante> vacantesDisponibles = vacanteService.obtenerVacantesActivas();
        model.addAttribute("vacantes", vacantesDisponibles);
        model.addAttribute("aspirante", aspirante);
        return "aspirante/vacantes-disponibles";
    }

    // ================= ASPIRANTE: POSTULARSE A VACANTE ESPECÍFICA =================
    @GetMapping("/aspirante/postular/{vacanteId}")
    public String mostrarPostulacionIndividual(@PathVariable Long vacanteId,
                                               HttpSession session,
                                               Model model) {
        Aspirante aspirante = obtenerAspiranteDeSesion(session);
        if (aspirante == null) {
            return "redirect:/auth/login";
        }

        Vacante vacante = vacanteService.obtenerPorId(vacanteId);
        if (vacante == null || vacante.getEstado() != EstadoVacante.PUBLICADA) {
            return "redirect:/solicitudes/aspirante/vacantes-disponibles";
        }

        model.addAttribute("vacante", vacante);
        model.addAttribute("aspirante", aspirante);
        return "aspirante/postular-vacantes";
    }

    @PostMapping("/aspirante/postular/{vacanteId}")
    public String postularAVacanteIndividual(@PathVariable Long vacanteId,
                                             @RequestParam("cvArchivo") MultipartFile cvArchivo,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        Aspirante aspirante = obtenerAspiranteDeSesion(session);
        if (aspirante == null) {
            return "redirect:/auth/login";
        }

        try {
            if (cvArchivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debes subir un archivo CV");
                return "redirect:/solicitudes/aspirante/postular/" + vacanteId;
            }

            solicitudService.crearSolicitudConArchivo(aspirante, vacanteId, cvArchivo);
            redirectAttributes.addFlashAttribute("success", "¡Solicitud enviada correctamente!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al postularse: " + e.getMessage());
        }

        return "redirect:/solicitudes/aspirante/mis-solicitudes";
    }

    // ================= ASPIRANTE: VER MIS SOLICITUDES =================
    @GetMapping("/aspirante/mis-solicitudes")
    public String verMisSolicitudes(HttpSession session, Model model) {
        Aspirante aspirante = obtenerAspiranteDeSesion(session);
        if (aspirante == null) {
            return "redirect:/auth/login";
        }

        List<Solicitud> solicitudes = solicitudService.obtenerSolicitudesPorAspirante(aspirante);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("aspirante", aspirante);
        return "aspirante/mis-solicitudes";
    }

    // ================= EMPLEADOR: VER SOLICITUDES POR VACANTE =================
    @GetMapping("/empleador/solicitudes")
    public String verSolicitudesEmpleador(@RequestParam(required = false) Long vacanteId,
                                          HttpSession session,
                                          Model model) {
        Empleador empleador = obtenerEmpleadorDeSesion(session);
        if (empleador == null) {
            return "redirect:/auth/login";
        }

        List<Vacante> vacantesDelEmpleador = vacanteService.obtenerPorEmpleador(empleador);

        List<VacanteConSolicitudes> vacantesConEstadisticas = new ArrayList<>();
        for (Vacante vacante : vacantesDelEmpleador) {
            long totalSolicitudes = solicitudService.contarSolicitudesPorVacante(vacante);
            long pendientes = solicitudService.contarSolicitudesPorVacanteYEstado(vacante, EstadoSolicitud.PENDIENTE);
            long aceptadas = solicitudService.contarSolicitudesPorVacanteYEstado(vacante, EstadoSolicitud.ACEPTADA);

            vacantesConEstadisticas.add(new VacanteConSolicitudes(vacante, totalSolicitudes, pendientes, aceptadas));
        }

        List<Solicitud> solicitudes;
        if (vacanteId != null) {
            solicitudes = solicitudService.obtenerSolicitudesPorVacante(vacanteId);
        } else {
            solicitudes = solicitudService.obtenerSolicitudesPorEmpleador(empleador);
        }

        long solicitudesPendientes = solicitudService.contarSolicitudesPendientesPorEmpleador(empleador);

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("empleador", empleador);
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        model.addAttribute("vacantes", vacantesConEstadisticas);
        model.addAttribute("vacanteSeleccionada", vacanteId);

        return "empleador/solicitudes";
    }

    // ================= EMPLEADOR: CAMBIAR ESTADO DE SOLICITUD =================
    @PostMapping("/empleador/cambiar-estado")
    public String cambiarEstadoSolicitud(@RequestParam Long solicitudId,
                                         @RequestParam EstadoSolicitud nuevoEstado,
                                         @RequestParam(required = false) String notaReclutador,
                                         @RequestParam(required = false) Long vacanteId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Empleador empleador = obtenerEmpleadorDeSesion(session);
        if (empleador == null) {
            return "redirect:/auth/login";
        }

        try {
            if (nuevoEstado == EstadoSolicitud.ACEPTADA) {
                solicitudService.aceptarSolicitudYRechazarOtras(solicitudId, notaReclutador);
                redirectAttributes.addFlashAttribute("success", "Solicitud aceptada. Las demás solicitudes para esta vacante han sido rechazadas automáticamente.");
            } else {
                solicitudService.cambiarEstadoSolicitud(solicitudId, nuevoEstado, notaReclutador);
                redirectAttributes.addFlashAttribute("success", "Estado de solicitud actualizado correctamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar estado: " + e.getMessage());
        }

        if (vacanteId != null) {
            return "redirect:/solicitudes/empleador/solicitudes?vacanteId=" + vacanteId;
        }
        return "redirect:/solicitudes/empleador/solicitudes";
    }

    // ================= DESCARGAR CV =================
    @GetMapping("/descargar-cv/{solicitudId}")
    public void descargarCV(@PathVariable Long solicitudId, HttpServletResponse response) {
        try {
            Solicitud solicitud = solicitudService.obtenerSolicitudPorId(solicitudId);

            if (solicitud == null || solicitud.getCvArchivo() == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "CV no encontrado");
                return;
            }

            String nombreAspirante = solicitud.getAspirante().getNombre().replace(" ", "_");
            String nombreDescarga = "CV_" + nombreAspirante + ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreDescarga + "\"");
            response.setContentLength(solicitud.getCvArchivo().length);

            response.getOutputStream().write(solicitud.getCvArchivo());
            response.getOutputStream().flush();

        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al descargar CV");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ================= CONFIRMACIÓN DE ENTREVISTA =================
    @GetMapping("/{id}/confirmar")
    public String confirmarEntrevista(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // ENVIAR EMAIL AL ASPIRANTE para que confirme
            enviarEmailConfirmacion(solicitud);

            redirectAttributes.addFlashAttribute("success", "Se ha enviado un correo al aspirante para confirmar la entrevista");
            return "redirect:/solicitudes/empleador/solicitudes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la confirmación: " + e.getMessage());
            return "redirect:/solicitudes/empleador/solicitudes";
        }
    }

    private void enviarEmailConfirmacion(Solicitud solicitud) {
        String emailAspirante = solicitud.getAspirante().getCorreo();
        String nombreAspirante = solicitud.getAspirante().getNombre();
        String tituloVacante = solicitud.getVacante().getTitulo();

        String enlaceConfirmacion = "http://localhost:8080/solicitudes/" + solicitud.getId() + "/confirmar-entrevista";

        // USA EL NUEVO MÉTODO CORRECTO
        emailTemplateService.enviarConfirmacionEntrevista(emailAspirante, nombreAspirante, tituloVacante, enlaceConfirmacion);
    }

    // Este método es para cuando el aspirante hace clic en el enlace del email
    @GetMapping("/{id}/confirmar-entrevista")
    public String aspiranteConfirmaEntrevista(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // Cambiar estado a ENTREVISTA_CONFIRMADA
            solicitud.setEstado(EstadoSolicitud.ACEPTADA);
            solicitudRepository.save(solicitud);

            redirectAttributes.addFlashAttribute("success", "¡Has confirmado tu asistencia a la entrevista! Te contactaremos pronto con los detalles.");
            return "redirect:/solicitudes/aspirante/mis-solicitudes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar la entrevista: " + e.getMessage());
            return "redirect:/solicitudes/aspirante/mis-solicitudes";
        }
    }

    // ================= MÉTODOS PRIVADOS =================
    private Aspirante obtenerAspiranteDeSesion(HttpSession session) {
        Object usuario = session.getAttribute("usuario");
        if (usuario instanceof Aspirante) {
            return (Aspirante) usuario;
        }
        return null;
    }

    private Empleador obtenerEmpleadorDeSesion(HttpSession session) {
        Object usuario = session.getAttribute("usuario");
        if (usuario instanceof Empleador) {
            return (Empleador) usuario;
        }
        return null;
    }

    // En tu AuthController o SolicitudController - TEMPORAL
    @GetMapping("/reenviar-notificaciones/{vacanteId}")
    @ResponseBody
    public String reenviarNotificaciones(@PathVariable Long vacanteId) {
        try {
            solicitudService.reenviarNotificacionesVacanteCerrada(vacanteId);
            return "✅ Notificaciones reenviadas exitosamente para la vacante ID: " + vacanteId;
        } catch (Exception e) {
            return "❌ Error reenviando notificaciones: " + e.getMessage();
        }
    }
}
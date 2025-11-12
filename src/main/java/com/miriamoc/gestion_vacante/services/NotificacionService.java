package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.Aspirante;
import com.miriamoc.gestion_vacante.models.Empleador;
import com.miriamoc.gestion_vacante.models.Vacante;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Service
public class NotificacionService {

    private final EmailTemplateService emailService;

    public NotificacionService(EmailTemplateService emailService) {
        this.emailService = emailService;
    }

    // METODO DE REDIRECCIÓN DIRECTAMENTE EN ESTA CLASE
    private String obtenerCorreoDestino(String correoOriginal) {
        // Si el correo termina en @gmail.com o @outlook.com, mantener el correo original
        if (correoOriginal != null &&
                (correoOriginal.toLowerCase().endsWith("@gmail.com") ||
                        correoOriginal.toLowerCase().endsWith("@outlook.com"))) {
            return correoOriginal; // Mantener el correo original
        }
        // Para cualquier otro dominio, redirigir a miriort.23@gmail.com
        return "miriort.23@gmail.com";
    }

    public void notificarAceptacion(Aspirante aspirante, Vacante vacante, String enlaceConfirmacion) {

        // ✅ APLICAR REDIRECCIÓN INTELIGENTE
        String correoDestino = obtenerCorreoDestino(aspirante.getCorreo());

        try {
            Context context = new Context();
            context.setVariable("nombreAspirante", aspirante.getNombre());
            context.setVariable("nombreVacante", vacante.getTitulo());
            context.setVariable("nombreEmpresa", vacante.getEmpleador().getEmpresa());
            context.setVariable("enlaceConfirmacion", enlaceConfirmacion);

            emailService.enviarEmailHtml(
                    correoDestino, // ✅ USA CORREO REDIRIGIDO
                    "🎉 ¡Felicidades! Has sido aceptado - " + vacante.getTitulo(),
                    "email/aceptacion",
                    context
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void notificarEntrevista(Aspirante aspirante, Vacante vacante,
                                    LocalDateTime fechaEntrevista, String enlaceReunion) {
        // ✅ APLICAR REDIRECCIÓN INTELIGENTE
        String correoDestino = obtenerCorreoDestino(aspirante.getCorreo());

        Context context = new Context();
        context.setVariable("nombreAspirante", aspirante.getNombre());
        context.setVariable("nombreVacante", vacante.getTitulo());
        context.setVariable("fechaEntrevista",
                fechaEntrevista.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")));
        context.setVariable("duracion", "30-45 minutos");
        context.setVariable("enlaceReunion", enlaceReunion);

        emailService.enviarEmailHtml(
                correoDestino, // ✅ USA CORREO REDIRIGIDO
                "🎯 Confirmación de Entrevista - " + vacante.getTitulo(),
                "email/entrevista",
                context
        );
    }

    public void notificarRechazo(Aspirante aspirante, Vacante vacante) {
        // ✅ APLICAR REDIRECCIÓN INTELIGENTE
        String correoDestino = obtenerCorreoDestino(aspirante.getCorreo());

        Context context = new Context();
        context.setVariable("nombreAspirante", aspirante.getNombre());
        context.setVariable("nombreVacante", vacante.getTitulo());
        context.setVariable("nombreEmpresa", vacante.getEmpleador().getEmpresa());
        context.setVariable("enlaceVacantes", "http://localhost:8080/auth/aspirante/vacantes");

        emailService.enviarEmailHtml(
                correoDestino, // ✅ USA CORREO REDIRIGIDO
                "Actualización de tu solicitud - " + vacante.getTitulo(),
                "email/rechazo",
                context
        );
    }

    public void notificarEmpleadorConfirmacion(Empleador empleador, Aspirante aspirante, Vacante vacante) {

        // ✅ APLICAR REDIRECCIÓN INTELIGENTE
        String correoDestino = obtenerCorreoDestino(empleador.getCorreo());

        try {
            Context context = new Context();
            context.setVariable("nombreEmpleador", empleador.getNombre());
            context.setVariable("nombreAspirante", aspirante.getNombre());
            context.setVariable("nombreVacante", vacante.getTitulo());
            context.setVariable("correoAspirante", aspirante.getCorreo());

            emailService.enviarEmailHtml(
                    correoDestino,
                    "✅ Aspirante ha confirmado entrevista - " + vacante.getTitulo(),
                    "email/confirmacion-empleador", // ← ESTE TEMPLATE
                    context
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
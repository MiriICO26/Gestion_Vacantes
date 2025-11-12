package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.Solicitud;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailTemplateService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailTemplateService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void enviarEmailHtml(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Procesar template Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom("miriort.23@gmail.com"); // CAMBIA POR TU CORREO
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            System.out.println("✅ Email enviado a: " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    // MÉTODO NUEVO - Para enviar confirmación de entrevista
    public void enviarConfirmacionEntrevista(String emailAspirante, String nombreAspirante, String tituloVacante, String enlaceConfirmacion) {
        Context context = new Context();
        context.setVariable("nombreAspirante", nombreAspirante);
        context.setVariable("tituloVacante", tituloVacante);
        context.setVariable("enlaceConfirmacion", enlaceConfirmacion);

        String asunto = "Confirmación de Entrevista - " + tituloVacante;

        enviarEmailHtml(emailAspirante, asunto, "email/confirmacion-entrevista", context);
    }

    // Puedes eliminar estos métodos si no los usas
    public void enviarEmailEntrevista(Solicitud solicitud) {
        // Este método puedes eliminarlo si no lo usas
    }

    public void enviarEmailEntrevista(String emailAspirante, String nombreAspirante, String tituloVacante, String enlaceConfirmacion) {
        // Este método puedes eliminarlo si no lo usas
    }
}
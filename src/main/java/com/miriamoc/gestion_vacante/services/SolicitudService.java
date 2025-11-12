package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.*;
import com.miriamoc.gestion_vacante.repositories.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final VacanteServiceI vacanteService;
    private final NotificacionService notificacionService; // ✅ CAMBIO: NotificacionService en lugar de EmailService

    public SolicitudService(SolicitudRepository solicitudRepository,
                            VacanteServiceI vacanteService,
                            NotificacionService notificacionService) { // ✅ CAMBIO
        this.solicitudRepository = solicitudRepository;
        this.vacanteService = vacanteService;
        this.notificacionService = notificacionService; // ✅ CAMBIO
    }

    // ================= MÉTODO NUEVO - ACEPTA MultipartFile =================
    @Transactional
    public void crearSolicitudConArchivo(Aspirante aspirante, Long vacanteId, MultipartFile cvArchivo) {
        try {
            // Verificar si ya existe una solicitud para esta vacante
            if (solicitudRepository.findByAspiranteAndVacanteId(aspirante.getId(), vacanteId).isPresent()) {
                throw new RuntimeException("Ya te has postulado a esta vacante");
            }

            // Obtener la vacante
            Vacante vacante = vacanteService.obtenerPorId(vacanteId);
            if (vacante == null) {
                throw new RuntimeException("La vacante no existe");
            }

            // Verificar que la vacante esté PUBLICADA
            if (vacante.getEstado() != EstadoVacante.PUBLICADA) {
                throw new RuntimeException("La vacante no está disponible para postulación");
            }

            // Crear la solicitud
            Solicitud solicitud = new Solicitud();
            solicitud.setAspirante(aspirante);
            solicitud.setVacante(vacante);
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
            solicitud.setActiva(true);

            // Procesar archivo CV
            if (cvArchivo != null && !cvArchivo.isEmpty()) {
                // Guardar nombre original
                String nombreOriginal = cvArchivo.getOriginalFilename();
                solicitud.setCvAdjunto(nombreOriginal != null ? nombreOriginal : "CV.pdf");

                // Guardar contenido del archivo en BD
                solicitud.setCvArchivo(cvArchivo.getBytes());
            } else {
                solicitud.setCvAdjunto("CV.pdf");
                solicitud.setCvArchivo(new byte[0]);
            }

            // Guardar en base de datos
            solicitudRepository.save(solicitud);

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo CV: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al crear solicitud: " + e.getMessage());
        }
    }

    // ================= MÉTODOS EXISTENTES =================
    @Transactional
    public Solicitud crearSolicitud(Aspirante aspirante, Long vacanteId, String cvArchivoNombre) {
        if (solicitudRepository.findByAspiranteAndVacanteId(aspirante.getId(), vacanteId).isPresent()) {
            throw new RuntimeException("Ya te has postulado a esta vacante");
        }

        Vacante vacante = vacanteService.obtenerPorId(vacanteId);
        if (vacante == null) {
            throw new RuntimeException("La vacante no existe");
        }

        if (vacante.getEstado() != EstadoVacante.PUBLICADA) {
            throw new RuntimeException("La vacante no está disponible para postulación");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setAspirante(aspirante);
        solicitud.setVacante(vacante);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setCvAdjunto(cvArchivoNombre);
        solicitud.setActiva(true);

        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public void postularAVacantes(Aspirante aspirante, List<Long> vacanteIds, String cvAdjunto) {
        for (Long vacanteId : vacanteIds) {
            try {
                crearSolicitud(aspirante, vacanteId, cvAdjunto);
            } catch (Exception e) {
                System.err.println("Error al postular a vacante " + vacanteId + ": " + e.getMessage());
            }
        }
    }

    public List<Solicitud> obtenerSolicitudesPorAspirante(Aspirante aspirante) {
        return solicitudRepository.findByAspiranteAndActivaTrueOrderByFechaSolicitudDesc(aspirante);
    }

    public List<Solicitud> obtenerSolicitudesPorEmpleador(Empleador empleador) {
        return solicitudRepository.findByVacanteEmpleadorAndActivaTrue(empleador);
    }

    @Transactional
    public void cambiarEstadoSolicitud(Long solicitudId, EstadoSolicitud nuevoEstado, String notaReclutador) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setEstado(nuevoEstado);
        if (notaReclutador != null && !notaReclutador.trim().isEmpty()) {
            solicitud.setNotaReclutador(notaReclutador);
        }
        solicitudRepository.save(solicitud);
    }

    public long contarSolicitudesPendientesPorEmpleador(Empleador empleador) {
        return solicitudRepository.countByVacanteEmpleadorAndEstadoAndActivaTrue(empleador, EstadoSolicitud.PENDIENTE);
    }

    public boolean existeSolicitud(Aspirante aspirante, Long vacanteId) {
        return solicitudRepository.findByAspiranteAndVacanteId(aspirante.getId(), vacanteId).isPresent();
    }

    public long contarSolicitudesPorVacante(Vacante vacante) {
        return solicitudRepository.countByVacanteAndActivaTrue(vacante);
    }

    public long contarSolicitudesPorVacanteYEstado(Vacante vacante, EstadoSolicitud estado) {
        return solicitudRepository.countByVacanteAndEstadoAndActivaTrue(vacante, estado);
    }

    public List<Solicitud> obtenerSolicitudesPorVacante(Long vacanteId) {
        Vacante vacante = new Vacante();
        vacante.setId(vacanteId);
        return solicitudRepository.findByVacanteAndActivaTrue(vacante);
    }

    // ================= MÉTODO ACTUALIZADO - CON NUEVO SISTEMA DE NOTIFICACIONES =================
    @Transactional
    public void aceptarSolicitudYRechazarOtras(Long solicitudIdAceptada, String notaReclutador) {
        // Obtener la solicitud aceptada
        Solicitud solicitudAceptada = solicitudRepository.findById(solicitudIdAceptada)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Vacante vacante = solicitudAceptada.getVacante();
        Aspirante aspiranteAceptado = solicitudAceptada.getAspirante();

        // Aceptar la solicitud seleccionada
        solicitudAceptada.setEstado(EstadoSolicitud.ACEPTADA);
        if (notaReclutador != null && !notaReclutador.trim().isEmpty()) {
            solicitudAceptada.setNotaReclutador(notaReclutador);
        }
        solicitudRepository.save(solicitudAceptada);

        // ✅ ENVIAR CORREO DE ACEPTACIÓN AL ASPIRANTE SELECCIONADO (NUEVO SISTEMA)
        try {
            String enlaceConfirmacion = "http://localhost:8080/solicitudes/" + solicitudIdAceptada + "/confirmar";
            notificacionService.notificarAceptacion(aspiranteAceptado, vacante, enlaceConfirmacion);
            System.out.println("✅ Correo de aceptación enviado a: " + aspiranteAceptado.getCorreo());
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de aceptación: " + e.getMessage());
        }

        // Rechazar automáticamente las demás solicitudes de la misma vacante
        List<Solicitud> otrasSolicitudes = solicitudRepository.findByVacanteAndActivaTrue(vacante);

        for (Solicitud solicitud : otrasSolicitudes) {
            if (!solicitud.getId().equals(solicitudIdAceptada)) {
                solicitud.setEstado(EstadoSolicitud.RECHAZADA);
                solicitud.setNotaReclutador("Rechazada automáticamente - Vacante asignada a otro candidato");
                solicitudRepository.save(solicitud);

                // ✅ ENVIAR CORREO DE RECHAZO AUTOMÁTICO A LOS DEMÁS ASPIRANTES (NUEVO SISTEMA)
                try {
                    notificacionService.notificarRechazo(solicitud.getAspirante(), vacante);
                    System.out.println("✅ Correo de rechazo enviado a: " + solicitud.getAspirante().getCorreo());
                } catch (Exception e) {
                    System.err.println("❌ Error enviando correo de rechazo: " + e.getMessage());
                }
            }
        }
    }

    // ================= NUEVO MÉTODO - CONFIRMAR ENTREVISTA =================
    @Transactional
    public void confirmarEntrevista(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Cambiar estado a CONFIRMADO
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepository.save(solicitud);

        // ✅ ENVIAR CORREO DE ENTREVISTA (NUEVO SISTEMA)
        try {
            LocalDateTime fechaEntrevista = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
            String enlaceReunion = "https://meet.google.com/abc-defg-hij"; // Cambiar por enlace real

            notificacionService.notificarEntrevista(
                    solicitud.getAspirante(),
                    solicitud.getVacante(),
                    fechaEntrevista,
                    enlaceReunion
            );
            System.out.println("✅ Correo de entrevista enviado a: " + solicitud.getAspirante().getCorreo());
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de entrevista: " + e.getMessage());
        }
    }

    public Solicitud obtenerSolicitudPorId(Long id) {
        return solicitudRepository.findById(id).orElse(null);
    }

    // En SolicitudService.java - AGREGA ESTE MÉTODO
    @Transactional
    public void reenviarNotificacionesVacanteCerrada(Long vacanteId) {
        System.out.println("🔄 REENVIANDO NOTIFICACIONES PARA VACANTE CERRADA: " + vacanteId);

        // Obtener la vacante
        Vacante vacante = vacanteService.obtenerPorId(vacanteId);
        if (vacante == null) {
            throw new RuntimeException("Vacante no encontrada");
        }

        // Buscar la solicitud ACEPTADA (el seleccionado)
        List<Solicitud> solicitudes = solicitudRepository.findByVacanteAndActivaTrue(vacante);
        Solicitud solicitudAceptada = solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.ACEPTADA)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró solicitud aceptada para esta vacante"));

        Aspirante aspiranteAceptado = solicitudAceptada.getAspirante();

        // ✅ REENVIAR CORREO DE ACEPTACIÓN AL SELECCIONADO
        try {
            String enlaceConfirmacion = "http://localhost:8080/solicitudes/" + solicitudAceptada.getId() + "/confirmar";
            notificacionService.notificarAceptacion(aspiranteAceptado, vacante, enlaceConfirmacion);
            System.out.println("✅✅✅ CORREO DE ACEPTACIÓN REENVIADO A: " + aspiranteAceptado.getCorreo());
        } catch (Exception e) {
            System.err.println("❌ Error reenviando aceptación: " + e.getMessage());
        }

        // ✅ REENVIAR CORREOS DE RECHAZO A LOS NO SELECCIONADOS
        List<Aspirante> aspirantesRechazados = solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.RECHAZADA)
                .map(Solicitud::getAspirante)
                .collect(Collectors.toList());

        for (Aspirante aspiranteRechazado : aspirantesRechazados) {
            try {
                notificacionService.notificarRechazo(aspiranteRechazado, vacante);
                System.out.println("✅✅✅ CORREO DE RECHAZO REENVIADO A: " + aspiranteRechazado.getCorreo());
            } catch (Exception e) {
                System.err.println("❌ Error reenviando rechazo: " + e.getMessage());
            }
        }

        System.out.println("🎉 REENVÍO DE NOTIFICACIONES COMPLETADO PARA VACANTE: " + vacante.getTitulo());
    }


}
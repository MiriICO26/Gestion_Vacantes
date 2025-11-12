package com.miriamoc.gestion_vacante.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aspirante_id", nullable = false)
    private Aspirante aspirante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id", nullable = false)
    private Vacante vacante;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "nota_reclutador", length = 1000)
    private String notaReclutador;

    @Column(name = "cv_adjunto") // Este ya lo tenías
    private String cvAdjunto;     // Para el nombre del archivo

    @Lob
    @Column(name = "cv_archivo", columnDefinition = "LONGBLOB")
    private byte[] cvArchivo;     // ✅ NUEVO: para el contenido del archivo

    @Column(nullable = false)
    private boolean activa = true;
}
package com.miriamoc.gestion_vacante.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacantes")
@Getter
@Setter
@NoArgsConstructor
public class Vacante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleador_id", nullable = false)
    private Empleador empleador;

    @NotBlank(message = "El título de la vacante es obligatorio")
    @Column(nullable = false, length = 255)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank(message = "Coloca al menos 3 requisitos")
    @Column(columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "ubicacion_id", length = 100)
    private String ubicacionId;

    @NotNull(message = "Selecciona un tipo de trabajo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajo", nullable = false)
    private TipoTrabajo tipoTrabajo;

    @NotNull(message = "El salario es obligatorio")
    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVacante estado;

}
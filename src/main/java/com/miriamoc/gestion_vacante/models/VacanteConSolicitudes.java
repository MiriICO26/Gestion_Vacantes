package com.miriamoc.gestion_vacante.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VacanteConSolicitudes {
    private Vacante vacante;
    private long totalSolicitudes;
    private long solicitudesPendientes;
    private long solicitudesAceptadas; // NUEVO CAMPO

    public VacanteConSolicitudes(Vacante vacante, long totalSolicitudes, long solicitudesPendientes) {
        this.vacante = vacante;
        this.totalSolicitudes = totalSolicitudes;
        this.solicitudesPendientes = solicitudesPendientes;
        this.solicitudesAceptadas = 0; // Valor por defecto
    }

    // Constructor con todos los campos
    public VacanteConSolicitudes(Vacante vacante, long totalSolicitudes, long solicitudesPendientes, long solicitudesAceptadas) {
        this.vacante = vacante;
        this.totalSolicitudes = totalSolicitudes;
        this.solicitudesPendientes = solicitudesPendientes;
        this.solicitudesAceptadas = solicitudesAceptadas;
    }
}
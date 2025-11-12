package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.Empleador;
import com.miriamoc.gestion_vacante.models.Vacante;
import java.util.List;

public interface VacanteServiceI {
    List<Vacante> obtenerTodas();
    List<Vacante> obtenerPorEmpleador(Empleador empleador);
    Vacante obtenerPorId(Long id);
    Vacante guardar(Vacante vacante);
    void eliminar(Long id);
    List<Vacante> obtenerVacantesActivas();
}
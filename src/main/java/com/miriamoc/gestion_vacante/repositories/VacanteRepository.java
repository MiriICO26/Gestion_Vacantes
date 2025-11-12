package com.miriamoc.gestion_vacante.repositories;

import com.miriamoc.gestion_vacante.models.Empleador;
import com.miriamoc.gestion_vacante.models.EstadoVacante;
import com.miriamoc.gestion_vacante.models.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VacanteRepository extends JpaRepository<Vacante, Long> {
    List<Vacante> findByEmpleador(Empleador empleador);
    List<Vacante> findByEmpleadorAndEstado(Empleador empleador, String estado);
    List<Vacante> findByFechaCierreBeforeAndEstado(LocalDateTime fecha, EstadoVacante estado);
    @Query("SELECT v FROM Vacante v WHERE v.fechaCierre < :now AND v.estado = 'PUBLICADA'")
    List<Vacante> findVacantesExpiradas(@Param("now") LocalDateTime now);
    List<Vacante> findByEstado(EstadoVacante estado);
}
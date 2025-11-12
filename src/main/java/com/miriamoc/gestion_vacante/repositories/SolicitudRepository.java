package com.miriamoc.gestion_vacante.repositories;

import com.miriamoc.gestion_vacante.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Encontrar solicitudes por aspirante
    List<Solicitud> findByAspiranteAndActivaTrueOrderByFechaSolicitudDesc(Aspirante aspirante);

    // Encontrar solicitudes por empleador
    List<Solicitud> findByVacanteEmpleadorAndActivaTrue(Empleador empleador);

    // Verificar si ya existe una solicitud del aspirante para la vacante
    @Query("SELECT s FROM Solicitud s WHERE s.aspirante.id = :aspiranteId AND s.vacante.id = :vacanteId AND s.activa = true")
    Optional<Solicitud> findByAspiranteAndVacanteId(@Param("aspiranteId") Long aspiranteId, @Param("vacanteId") Long vacanteId);

    // Contar solicitudes por estado para un empleador
    long countByVacanteEmpleadorAndEstadoAndActivaTrue(Empleador empleador, EstadoSolicitud estado);

    // Encontrar solicitudes por vacante
    List<Solicitud> findByVacanteAndActivaTrue(Vacante vacante);

    // Nuevos métodos para estadísticas
    long countByVacanteAndActivaTrue(Vacante vacante);

    long countByVacanteAndEstadoAndActivaTrue(Vacante vacante, EstadoSolicitud estado);

    List<Solicitud> findByVacanteAndEstadoAndActivaTrue(Vacante vacante, EstadoSolicitud estado);
}
package com.miriamoc.gestion_vacante.repositories;

import com.miriamoc.gestion_vacante.models.Empleador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpleadorRepository extends JpaRepository<Empleador, Long> {
    Optional<Empleador> findByCorreo(String correo);
}
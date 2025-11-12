package com.miriamoc.gestion_vacante.repositories;

import com.miriamoc.gestion_vacante.models.Aspirante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AspiranteRepository extends JpaRepository<Aspirante, Long> {
    Optional<Aspirante> findByCorreo(String correo);
}
package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.Empleador;
import com.miriamoc.gestion_vacante.models.EstadoVacante;
import com.miriamoc.gestion_vacante.models.Vacante;
import com.miriamoc.gestion_vacante.repositories.VacanteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VacanteServiceImplementacion implements VacanteServiceI {

    private final VacanteRepository vacanteRepository;

    public VacanteServiceImplementacion(VacanteRepository vacanteRepository) {
        this.vacanteRepository = vacanteRepository;
    }

    @Override
    public List<Vacante> obtenerTodas() {
        return vacanteRepository.findAll();
    }

    @Override
    public List<Vacante> obtenerPorEmpleador(Empleador empleador) {
        return vacanteRepository.findByEmpleador(empleador);
    }

    @Override
    public Vacante obtenerPorId(Long id) {
        return vacanteRepository.findById(id).orElse(null);
    }

    @Override
    public Vacante guardar(Vacante vacante) {
        actualizarEstadoSegunFechaCierre(vacante);
        return vacanteRepository.save(vacante);
    }

    @Override
    public void eliminar(Long id) {
        vacanteRepository.deleteById(id);
    }

    @Override
    public List<Vacante> obtenerVacantesActivas() {
        return vacanteRepository.findByEstado(EstadoVacante.PUBLICADA);
    }

    public void actualizarEstadoSegunFechaCierre(Vacante vacante) {
        if (vacante.getFechaCierre() != null) {
            LocalDateTime ahora = LocalDateTime.now();
            if (ahora.isAfter(vacante.getFechaCierre())) {
                vacante.setEstado(EstadoVacante.CERRADA);
            } else {
                vacante.setEstado(EstadoVacante.PUBLICADA);
            }
        } else if (vacante.getEstado() == null) {
            vacante.setEstado(EstadoVacante.PUBLICADA);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cerrarVacantesConFechaExpirada() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Vacante> vacantesExpiradas = vacanteRepository.findByFechaCierreBeforeAndEstado(ahora, EstadoVacante.PUBLICADA);

        for (Vacante vacante : vacantesExpiradas) {
            vacante.setEstado(EstadoVacante.CERRADA);
            vacanteRepository.save(vacante);
        }
    }
}


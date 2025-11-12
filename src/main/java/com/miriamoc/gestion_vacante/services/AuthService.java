package com.miriamoc.gestion_vacante.services;

import com.miriamoc.gestion_vacante.models.Aspirante;
import com.miriamoc.gestion_vacante.models.Empleador;
import com.miriamoc.gestion_vacante.models.Usuario;
import com.miriamoc.gestion_vacante.repositories.AspiranteRepository;
import com.miriamoc.gestion_vacante.repositories.EmpleadorRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AspiranteRepository aspiranteRepository;
    private final EmpleadorRepository empleadorRepository;

    public AuthService(AspiranteRepository aspiranteRepository,
                       EmpleadorRepository empleadorRepository) {
        this.aspiranteRepository = aspiranteRepository;
        this.empleadorRepository = empleadorRepository;
    }

    // Hash de la contraseña usando BCrypt
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verifica que la contraseña proporcionada coincida con el hash
    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    // Autentica al usuario según correo y contraseña
    public Usuario autenticar(String correo, String password) {
        // 1. Buscar primero en Aspirantes
        Optional<Aspirante> aspiranteOpt = aspiranteRepository.findByCorreo(correo);
        if (aspiranteOpt.isPresent()) {
            Aspirante aspirante = aspiranteOpt.get();
            if (verifyPassword(password, aspirante.getPassword())) {
                return aspirante; // Retorna el Aspirante (que es un Usuario)
            }
        }

        // 2. Buscar en Empleadores
        Optional<Empleador> empleadorOpt = empleadorRepository.findByCorreo(correo);
        if (empleadorOpt.isPresent()) {
            Empleador empleador = empleadorOpt.get();
            if (verifyPassword(password, empleador.getPassword())) {
                return empleador; // Retorna el Empleador (que es un Usuario)
            }
        }

        return null; // No encontrado en ninguna tabla
    }
}
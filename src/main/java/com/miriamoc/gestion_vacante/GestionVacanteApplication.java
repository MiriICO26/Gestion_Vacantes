package com.miriamoc.gestion_vacante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestionVacanteApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionVacanteApplication.class, args);
        System.out.println("Gestion de Vacantes ejecutandose en el puerto 8080...");
	}

}

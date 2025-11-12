package com.miriamoc.gestion_vacante.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "aspirantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aspirante extends Usuario {

    @NotBlank(message = "Debe colocar al menos 1 habilidad")
    @Column(length = 1000)
    private String habilidades;
}
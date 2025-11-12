package com.miriamoc.gestion_vacante.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "empleadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empleador extends Usuario {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(nullable = false)
    private String empresa;
}
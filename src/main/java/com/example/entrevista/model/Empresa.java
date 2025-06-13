package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;

    private String telefono;

    private String direccion;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Rol rol;
}

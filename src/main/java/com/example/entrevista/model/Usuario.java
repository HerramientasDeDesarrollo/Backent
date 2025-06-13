package com.example.entrevista.model;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private Date nacimiento;
    private int telefono;
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;
}

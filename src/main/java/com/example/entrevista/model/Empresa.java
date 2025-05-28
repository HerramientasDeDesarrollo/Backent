package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    
    @Column(unique = true)
    private String correo;
    
    private int telefono;
    private String password;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    private boolean creadaPorAdmin;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Entrevista> entrevistas;

}

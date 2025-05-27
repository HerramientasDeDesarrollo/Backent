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
public class Entrevista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String puesto;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaExpiracion;

    private boolean activo;

    @OneToMany(mappedBy = "entrevista", cascade = CascadeType.ALL)
    private List<Pregunta> preguntas;

}

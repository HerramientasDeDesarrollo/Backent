package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String claridadEstructura;
    private String dominioTecnico;
    private String pertinencia;
    private String comunicacionSeguridad;

    @Column(length = 1000) 
    private String fortalezas;

    @Column(length = 1000) 
    private String oportunidadesMejora;

    private Double puntuacionFinal;

    @ManyToOne
    @JoinColumn(name = "entrevista_id")
    private Entrevista entrevista;

    @ManyToOne
    @JoinColumn(name = "postulacion_id")
    private Postulacion postulacion;


}

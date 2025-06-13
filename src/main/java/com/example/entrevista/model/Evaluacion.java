package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "postulacion_id")
    private Postulacion postulacion;
    
    @ManyToOne
    @JoinColumn(name = "pregunta_id")
    private Pregunta pregunta;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEvaluacion;

    @Column(length = 5000)
    private String evaluacionCompleta; // JSON con toda la evaluación detallada
    
    @Column(length = 5000)
    private String respuesta; // Respuesta del candidato
    
    // Métricas de evaluación
    private Integer claridadEstructura;
    private Integer dominioTecnico;
    private Integer pertinencia;
    private Integer comunicacionSeguridad;
    private Double puntajeTotal; // Puntaje promedio (1-100)
    private Double porcentajeObtenido; // Porcentaje del valor máximo de la pregunta

    private String estado; // "completada", "error", etc.
}

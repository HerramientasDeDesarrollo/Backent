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

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEvaluacion;

    @Column(length = 5000)
    private String evaluacionCompleta; // JSON con toda la evaluación detallada

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double costoUSD;

    private String estado; // "completada", "error", etc.
}

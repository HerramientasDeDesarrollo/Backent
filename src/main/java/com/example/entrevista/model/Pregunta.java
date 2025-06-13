package com.example.entrevista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;

    private String textoPregunta;
    
    private int dificultad; // Nivel de dificultad de 1 a 10
    
    private int score; // Valor de la pregunta en porcentaje

    @ManyToOne
    @JoinColumn(name = "convocatoria_id")
    private Convocatoria convocatoria;

    @ManyToOne
    @JoinColumn(name = "postulacion_id")
    @JsonIgnore 
    private Postulacion postulacion;
}

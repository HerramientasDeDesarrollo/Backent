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
    
    private String tipo; // Tipo de pregunta (technical_knowledge, experience, etc.)
    
    private String tipoLegible; // Versión en español más legible (ej. "Conocimiento Técnico")
    
    private int score; // Valor de la pregunta en porcentaje

    @ManyToOne
    @JoinColumn(name = "convocatoria_id")
    private Convocatoria convocatoria;

    @ManyToOne
    @JoinColumn(name = "postulacion_id")
    @JsonIgnore 
    private Postulacion postulacion;
}

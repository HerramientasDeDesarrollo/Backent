package com.example.entrevista.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionRequest {

    private String question;
    private String answer;
    private String puesto; // Para contexto de la evaluación
    private Long idPostulacion; // Para asociar con la postulación
    private int valorPregunta;
}

package com.example.entrevista.DTO;

import java.util.List;

public class EvaluacionRequest {
    private String question;
    private String answer;
    private String puesto; // Para contexto de la evaluación
    private Long idPostulacion; // Para asociar con la postulación
    private int valorPregunta; // Valor/peso de la pregunta

    public EvaluacionRequest() {}

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public Long getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(Long idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

    public int getValorPregunta() {
        return valorPregunta;
    }

    public void setValorPregunta(int valorPregunta) {
        this.valorPregunta = valorPregunta;
    }
}

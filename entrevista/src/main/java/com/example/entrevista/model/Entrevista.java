package com.example.entrevista.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Entrevista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ElementCollection
    private List<String> preguntas;
    
    @ElementCollection
    private List<String> respuestas;
    
    private String evaluacion;
    private Double puntuacionFinal;
    private String fortalezas;
    private String oportunidadesMejora;

    // Getters
    public Long getId() {
        return id;
    }

    public List<String> getPreguntas() {
        return preguntas;
    }

    public List<String> getRespuestas() {
        return respuestas;
    }

    public String getEvaluacion() {
        return evaluacion;
    }

    public Double getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public String getFortalezas() {
        return fortalezas;
    }

    public String getOportunidadesMejora() {
        return oportunidadesMejora;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPreguntas(List<String> preguntas) {
        this.preguntas = preguntas;
    }

    public void setRespuestas(List<String> respuestas) {
        this.respuestas = respuestas;
    }

    public void setEvaluacion(String evaluacion) {
        this.evaluacion = evaluacion;
    }

    public void setPuntuacionFinal(Double puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }

    public void setFortalezas(String fortalezas) {
        this.fortalezas = fortalezas;
    }

    public void setOportunidadesMejora(String oportunidadesMejora) {
        this.oportunidadesMejora = oportunidadesMejora;
    }
}
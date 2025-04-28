package com.example.entrevista.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Entrevista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String preguntas;

    @Column(length = 1000) 
    private String respuestas;

    private String claridadEstructura;
    private String dominioTecnico;
    private String pertinencia;
    private String comunicacionSeguridad;

    @Column(length = 1000) 
    private String fortalezas;

    @Column(length = 1000) 
    private String oportunidadesMejora;

    private Double puntuacionFinal;

    // Constructor vacío
    public Entrevista() {}

    // Constructor completo
    public Entrevista(String preguntas, String respuestas, String claridadEstructura, String dominioTecnico,
                      String pertinencia, String comunicacionSeguridad, String fortalezas,
                      String oportunidadesMejora, Double puntuacionFinal) {
        this.preguntas = preguntas;
        this.respuestas = respuestas;
        this.claridadEstructura = claridadEstructura;
        this.dominioTecnico = dominioTecnico;
        this.pertinencia = pertinencia;
        this.comunicacionSeguridad = comunicacionSeguridad;
        this.fortalezas = fortalezas;
        this.oportunidadesMejora = oportunidadesMejora;
        this.puntuacionFinal = puntuacionFinal;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(String preguntas) {
        this.preguntas = preguntas;
    }

    public String getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(String respuestas) {
        this.respuestas = respuestas;
    }

    public String getClaridadEstructura() {
        return claridadEstructura;
    }

    public void setClaridadEstructura(String claridadEstructura) {
        this.claridadEstructura = claridadEstructura;
    }

    public String getDominioTecnico() {
        return dominioTecnico;
    }

    public void setDominioTecnico(String dominioTecnico) {
        this.dominioTecnico = dominioTecnico;
    }

    public String getPertinencia() {
        return pertinencia;
    }

    public void setPertinencia(String pertinencia) {
        this.pertinencia = pertinencia;
    }

    public String getComunicacionSeguridad() {
        return comunicacionSeguridad;
    }

    public void setComunicacionSeguridad(String comunicacionSeguridad) {
        this.comunicacionSeguridad = comunicacionSeguridad;
    }

    public String getFortalezas() {
        return fortalezas;
    }

    public void setFortalezas(String fortalezas) {
        this.fortalezas = fortalezas;
    }

    public String getOportunidadesMejora() {
        return oportunidadesMejora;
    }

    public void setOportunidadesMejora(String oportunidadesMejora) {
        this.oportunidadesMejora = oportunidadesMejora;
    }

    public Double getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(Double puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }
}
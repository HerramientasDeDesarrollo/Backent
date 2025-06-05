package com.example.entrevista.DTO;

import java.util.List;
import java.util.Map;

public class EvaluacionResponse {
    private boolean success;
    private String pregunta;
    private String respuesta;
    private int claridadEstructura;
    private int dominioTecnico;
    private int pertinencia;
    private int comunicacionSeguridad;
    private List<String> fortalezas;
    private List<String> oportunidadesMejora;
    private int puntuacionFinal;

    // Getters y setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public int getClaridadEstructura() {
        return claridadEstructura;
    }

    public void setClaridadEstructura(int claridadEstructura) {
        this.claridadEstructura = claridadEstructura;
    }

    public int getDominioTecnico() {
        return dominioTecnico;
    }

    public void setDominioTecnico(int dominioTecnico) {
        this.dominioTecnico = dominioTecnico;
    }

    public int getPertinencia() {
        return pertinencia;
    }

    public void setPertinencia(int pertinencia) {
        this.pertinencia = pertinencia;
    }

    public int getComunicacionSeguridad() {
        return comunicacionSeguridad;
    }

    public void setComunicacionSeguridad(int comunicacionSeguridad) {
        this.comunicacionSeguridad = comunicacionSeguridad;
    }

    public List<String> getFortalezas() {
        return fortalezas;
    }

    public void setFortalezas(List<String> fortalezas) {
        this.fortalezas = fortalezas;
    }

    public List<String> getOportunidadesMejora() {
        return oportunidadesMejora;
    }

    public void setOportunidadesMejora(List<String> oportunidadesMejora) {
        this.oportunidadesMejora = oportunidadesMejora;
    }

    public int getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(int puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }

    // Método estático para crear respuesta de error
    public static EvaluacionResponse error(String errorMessage) {
        EvaluacionResponse response = new EvaluacionResponse();
        response.setSuccess(false);
        return response;
    }
}
package com.example.entrevista.DTO;

public class PreguntaRequest {
    private String puesto;
    private Long idEntrevista;
    private Long idPostulacion; // Nuevo campo
    private int dificultad; // Valor del 1 al 10

    public PreguntaRequest() {}

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public Long getIdEntrevista() {
        return idEntrevista;
    }

    public void setIdEntrevista(Long idEntrevista) {
        this.idEntrevista = idEntrevista;
    }

    public Long getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(Long idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

    public int getDificultad() {
        return dificultad;
    }

    public void setDificultad(int dificultad) {
        this.dificultad = dificultad;
    }
}
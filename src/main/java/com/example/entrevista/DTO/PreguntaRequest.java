package com.example.entrevista.DTO;

public class PreguntaRequest {
    private String puesto;
    private Long idEntrevista;

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

}
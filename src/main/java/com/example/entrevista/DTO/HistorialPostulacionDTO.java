package com.example.entrevista.DTO;

import java.util.Date;

public class HistorialPostulacionDTO {
    private int ciclo;
    private Date fechaPostulacion;
    private String estado;
    private double puntuacionFinal; // Puede ser null si no hay resultado

    public int getCiclo() {
        return ciclo;
    }

    public void setCiclo(int ciclo) {
        this.ciclo = ciclo;
    }

    public Date getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(Date fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(double puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }
}

package com.example.entrevista.DTO;

import java.util.Objects;

public class Pregunta {
    private int numero;
    private String pregunta;

    // Constructores
    public Pregunta() {}

    public Pregunta(int numero, String pregunta) {
        this.numero = numero;
        this.pregunta = pregunta;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    @Override
    public String toString() {
        return "Pregunta{" +
                "numero=" + numero +
                ", pregunta='" + pregunta + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pregunta pregunta1 = (Pregunta) o;
        return numero == pregunta1.numero && Objects.equals(pregunta, pregunta1.pregunta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, pregunta);
    }
}

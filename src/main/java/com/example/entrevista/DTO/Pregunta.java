package com.example.entrevista.DTO;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;
    private String pregunta;

    // Constructores
    public Pregunta() {}

    public Pregunta(int numero, String pregunta) {
        this.numero = numero;
        this.pregunta = pregunta;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                "id=" + id +
                ", numero=" + numero +
                ", pregunta='" + pregunta + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pregunta pregunta1 = (Pregunta) o;
        return numero == pregunta1.numero && Objects.equals(id, pregunta1.id) && Objects.equals(pregunta, pregunta1.pregunta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numero, pregunta);
    }
}

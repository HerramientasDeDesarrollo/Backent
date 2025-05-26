package com.example.entrevista.model;

import jakarta.persistence.*;

@Entity
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;

    private String textoPregunta;

    @ManyToOne
    @JoinColumn(name = "entrevista_id")
    private Entrevista entrevista;

    public Pregunta() {}

    public Pregunta(int numero, String textoPregunta, Entrevista entrevista) {
        this.numero = numero;
        this.textoPregunta = textoPregunta;
        this.entrevista = entrevista;
    }

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

    public String getTextoPregunta() {
        return textoPregunta;
    }

    public void setTextoPregunta(String textoPregunta) {
        this.textoPregunta = textoPregunta;
    }

    public Entrevista getEntrevista() {
        return entrevista;
    }

    public void setEntrevista(Entrevista entrevista) {
        this.entrevista = entrevista;
    }
}

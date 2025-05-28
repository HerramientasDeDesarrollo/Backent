package com.example.entrevista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;

    private String textoPregunta;

    @ManyToOne
    @JoinColumn(name = "entrevista_id")
    private Entrevista entrevista;

    @ManyToOne
    @JoinColumn(name = "postulacion_id")
    @JsonIgnore 
    private Postulacion postulacion;
}

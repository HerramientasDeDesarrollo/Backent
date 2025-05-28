package com.example.entrevista.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Postulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "entrevista_id")
    private Entrevista entrevista;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaPostulacion;

    private String estado;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Pregunta> preguntas;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private List<Resultado> resultados;

    private int ciclo;
}

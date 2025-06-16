package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Convocatoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    
    @Lob
    private String puesto; //Promt para la IA

    private boolean activo; // Indica si la convocatoria está activa o no
    private String fechaPublicacion; // Fecha de publicación en formato ISO 8601 (YYYY-MM-DD)
    private String fechaCierre; // Fecha de cierre en formato ISO 8601 (YYYY-MM-DD)
    private int dificultad; // Nivel de dificultad de la convocatoria

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}

package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "convocatoria_id")
    private Convocatoria convocatoria;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private List<Pregunta> preguntas;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPostulacion estado = EstadoPostulacion.PENDIENTE; // Valor por defecto
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean preguntasGeneradas = false; // Nuevo campo para rastrear si se han generado preguntas
    
    // Referencia a la sesión de entrevista (nueva arquitectura)
    @Column(name = "entrevista_session_id")
    private Long entrevistaSessionId;
}

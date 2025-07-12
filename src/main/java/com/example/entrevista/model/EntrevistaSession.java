package com.example.entrevista.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrevista_sessions")
public class EntrevistaSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postulacion_id", nullable = false)
    private Postulacion postulacion;
    
    @Column(name = "preguntas_json", columnDefinition = "TEXT")
    private String preguntasJson;
    
    @Column(name = "respuestas_json", columnDefinition = "TEXT")
    private String respuestasJson;
    
    @Column(name = "evaluaciones_json", columnDefinition = "TEXT")
    private String evaluacionesJson;
    
    @Column(name = "estado_sesion")
    @Enumerated(EnumType.STRING)
    private EstadoSesion estadoSesion;
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_ultima_actividad")
    private LocalDateTime fechaUltimaActividad;
    
    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;
    
    @Column(name = "progreso_completado")
    private Integer progresoCompletado; // 0-100
    
    @Column(name = "puntuacion_total")
    private Double puntuacionTotal;
    
    @Column(name = "es_completada")
    private Boolean esCompletada;
    
    @Column(name = "metadatos_json", columnDefinition = "TEXT")
    private String metadatosJson; // Para información adicional
    
    // Constructores
    public EntrevistaSession() {}
    
    public EntrevistaSession(Postulacion postulacion) {
        this.postulacion = postulacion;
        this.estadoSesion = EstadoSesion.INICIADA;
        this.fechaInicio = LocalDateTime.now();
        this.fechaUltimaActividad = LocalDateTime.now();
        this.progresoCompletado = 0;
        this.esCompletada = false;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Postulacion getPostulacion() {
        return postulacion;
    }
    
    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }
    
    public String getPreguntasJson() {
        return preguntasJson;
    }
    
    public void setPreguntasJson(String preguntasJson) {
        this.preguntasJson = preguntasJson;
    }
    
    public String getRespuestasJson() {
        return respuestasJson;
    }
    
    public void setRespuestasJson(String respuestasJson) {
        this.respuestasJson = respuestasJson;
    }
    
    public String getEvaluacionesJson() {
        return evaluacionesJson;
    }
    
    public void setEvaluacionesJson(String evaluacionesJson) {
        this.evaluacionesJson = evaluacionesJson;
    }
    
    public EstadoSesion getEstadoSesion() {
        return estadoSesion;
    }
    
    public void setEstadoSesion(EstadoSesion estadoSesion) {
        this.estadoSesion = estadoSesion;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaUltimaActividad() {
        return fechaUltimaActividad;
    }
    
    public void setFechaUltimaActividad(LocalDateTime fechaUltimaActividad) {
        this.fechaUltimaActividad = fechaUltimaActividad;
    }
    
    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }
    
    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }
    
    public Integer getProgresoCompletado() {
        return progresoCompletado;
    }
    
    public void setProgresoCompletado(Integer progresoCompletado) {
        this.progresoCompletado = progresoCompletado;
    }
    
    public Double getPuntuacionTotal() {
        return puntuacionTotal;
    }
    
    public void setPuntuacionTotal(Double puntuacionTotal) {
        this.puntuacionTotal = puntuacionTotal;
    }
    
    public Boolean getEsCompletada() {
        return esCompletada;
    }
    
    public void setEsCompletada(Boolean esCompletada) {
        this.esCompletada = esCompletada;
    }
    
    public String getMetadatosJson() {
        return metadatosJson;
    }
    
    public void setMetadatosJson(String metadatosJson) {
        this.metadatosJson = metadatosJson;
    }
    
    // Métodos de utilidad
    public void actualizarUltimaActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }
    
    public void marcarComoCompletada() {
        this.esCompletada = true;
        this.fechaFinalizacion = LocalDateTime.now();
        this.estadoSesion = EstadoSesion.COMPLETADA;
        this.progresoCompletado = 100;
    }
    
    @Override
    public String toString() {
        return "EntrevistaSession{" +
                "id=" + id +
                ", postulacionId=" + (postulacion != null ? postulacion.getId() : null) +
                ", estadoSesion=" + estadoSesion +
                ", progreso=" + progresoCompletado +
                ", completada=" + esCompletada +
                '}';
    }
}

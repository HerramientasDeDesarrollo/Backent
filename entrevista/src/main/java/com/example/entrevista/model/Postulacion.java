package com.example.entrevista.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
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

    private Double puntuacionFinal;

    @OneToMany(mappedBy = "postulacion", cascade = CascadeType.ALL)
    private List<Resultado> resultados;

    public Postulacion() {}

    public Postulacion(Usuario usuario, Entrevista entrevista, Date fechaPostulacion, String estado, Double puntuacionFinal, List<Resultado> resultados) {
        this.usuario = usuario;
        this.entrevista = entrevista;
        this.fechaPostulacion = fechaPostulacion;
        this.estado = estado;
        this.puntuacionFinal = puntuacionFinal;
        this.resultados = resultados;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Entrevista getEntrevista() {
        return entrevista;
    }

    public void setEntrevista(Entrevista entrevista) {
        this.entrevista = entrevista;
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

    public Double getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(Double puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }

    public List<Resultado> getResultados() {
        return resultados;
    }

    public void setResultados(List<Resultado> resultados) {
        this.resultados = resultados;
    }
}

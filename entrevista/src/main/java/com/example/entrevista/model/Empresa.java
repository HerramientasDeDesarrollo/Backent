package com.example.entrevista.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String telefono;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    private boolean creadaPorAdmin;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Entrevista> entrevistas;

    public Empresa() {}

    public Empresa(String nombre, String email, String telefono, Date fechaRegistro, boolean creadaPorAdmin, List<Entrevista> entrevistas) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.creadaPorAdmin = creadaPorAdmin;
        this.entrevistas = entrevistas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isCreadaPorAdmin() {
        return creadaPorAdmin;
    }

    public void setCreadaPorAdmin(boolean creadaPorAdmin) {
        this.creadaPorAdmin = creadaPorAdmin;
    }

    public List<Entrevista> getEntrevistas() {
        return entrevistas;
    }

    public void setEntrevistas(List<Entrevista> entrevistas) {
        this.entrevistas = entrevistas;
    }
}

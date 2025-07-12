package com.example.entrevista.model;

public enum EstadoSesion {
    INICIADA("Entrevista iniciada"),
    EN_PROGRESO("En progreso"),
    PAUSADA("Pausada"),
    COMPLETADA("Completada"),
    ABANDONADA("Abandonada"),
    EXPIRADA("Expirada");
    
    private final String descripcion;
    
    EstadoSesion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}

package com.example.entrevista.security;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private final String email;
    private final Long userId;
    private final String nombre;
    private final String apellidoPaterno;
    private final String apellidoMaterno;

    public UserPrincipal(String email, Long userId, String nombre, String apellidoPaterno, String apellidoMaterno) {
        this.email = email;
        this.userId = userId;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null) nombreCompleto.append(nombre);
        if (apellidoPaterno != null) nombreCompleto.append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null) nombreCompleto.append(" ").append(apellidoMaterno);
        return nombreCompleto.toString().trim();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "email='" + email + '\'' +
                ", userId=" + userId +
                ", nombre='" + nombre + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                '}';
    }
}
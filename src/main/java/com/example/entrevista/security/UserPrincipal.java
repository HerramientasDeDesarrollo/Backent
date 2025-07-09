package com.example.entrevista.security;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private final String email;
    private final Long id; // Puede ser userId o empresaId
    private final String nombre;
    private final String apellidoPaterno;
    private final String apellidoMaterno;
    private final String userType; // "USUARIO" o "EMPRESA"

    public UserPrincipal(String email, Long id, String nombre, String apellidoPaterno, String apellidoMaterno) {
        this.email = email;
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        // Determinar tipo basado en si tiene apellidos o no
        this.userType = (apellidoPaterno != null || apellidoMaterno != null) ? "USUARIO" : "EMPRESA";
    }
    
    public UserPrincipal(String email, Long id, String nombre, String apellidoPaterno, String apellidoMaterno, String userType) {
        this.email = email;
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.userType = userType;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
    
    // Mantener compatibilidad con getUserId
    public Long getUserId() {
        return id;
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
    
    public String getUserType() {
        return userType;
    }
    
    public boolean isEmpresa() {
        return "EMPRESA".equals(userType);
    }
    
    public boolean isUsuario() {
        return "USUARIO".equals(userType);
    }

    public String getNombreCompleto() {
        if (isEmpresa()) {
            return nombre; // Para empresas, solo devolver el nombre de la empresa
        }
        // Para usuarios, construir nombre completo
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
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
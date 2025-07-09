package com.example.entrevista.DTO;

public class AuthResponse {
    private String jwt;
    private String userType; // "USUARIO" o "EMPRESA"
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    
    public AuthResponse(String jwt) { 
        this.jwt = jwt; 
    }
    
    public AuthResponse(String jwt, String userType, Long id, String nombre, String apellidoPaterno, String apellidoMaterno, String email) {
        this.jwt = jwt;
        this.userType = userType;
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.email = email;
    }
    
    // Getters y Setters
    public String getJwt() { return jwt; }
    public void setJwt(String jwt) { this.jwt = jwt; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNombreCompleto() {
        if (userType != null && userType.equals("EMPRESA")) {
            return nombre; // Para empresas, solo devolver el nombre de la empresa
        }
        // Para usuarios, construir nombre completo
        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null) nombreCompleto.append(nombre);
        if (apellidoPaterno != null) nombreCompleto.append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null) nombreCompleto.append(" ").append(apellidoMaterno);
        return nombreCompleto.toString().trim();
    }
}

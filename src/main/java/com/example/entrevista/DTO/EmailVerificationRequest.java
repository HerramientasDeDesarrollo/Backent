package com.example.entrevista.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para solicitar verificación de email
 */
public class EmailVerificationRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "El tipo de usuario es requerido")
    @Pattern(regexp = "^(candidato|empresa|admin)$", message = "Tipo de usuario inválido")
    private String userType;
    
    public EmailVerificationRequest() {}
    
    public EmailVerificationRequest(String email, String userType) {
        this.email = email;
        this.userType = userType;
    }
    
    // Getters y Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    @Override
    public String toString() {
        return "EmailVerificationRequest{" +
                "email='" + email + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}

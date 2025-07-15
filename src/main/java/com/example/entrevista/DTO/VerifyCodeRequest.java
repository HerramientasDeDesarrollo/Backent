package com.example.entrevista.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para validar código de verificación
 */
public class VerifyCodeRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "El código de verificación es requerido")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe ser de 6 dígitos")
    private String verificationCode;
    
    public VerifyCodeRequest() {}
    
    public VerifyCodeRequest(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }
    
    // Getters y Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    @Override
    public String toString() {
        return "VerifyCodeRequest{" +
                "email='" + email + '\'' +
                ", verificationCode='***'" + // No mostrar el código en logs
                '}';
    }
}

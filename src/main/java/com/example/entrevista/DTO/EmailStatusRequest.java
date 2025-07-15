package com.example.entrevista.DTO;

/**
 * Request para consultar estado de verificación de email de forma segura
 */
public class EmailStatusRequest {
    private String email;
    
    public EmailStatusRequest() {}
    
    public EmailStatusRequest(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}

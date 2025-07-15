package com.example.entrevista.DTO;

/**
 * DTO para respuesta de verificación de email
 */
public class EmailVerificationResponse {
    
    private boolean success;
    private String message;
    private String email;
    private int remainingAttempts;
    private long minutesUntilExpiry;
    private boolean hasActiveCode;
    
    // Constructor por defecto
    public EmailVerificationResponse() {}
    
    // Constructor para éxito
    public EmailVerificationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Constructor completo
    public EmailVerificationResponse(boolean success, String message, String email, 
                                   int remainingAttempts, long minutesUntilExpiry, boolean hasActiveCode) {
        this.success = success;
        this.message = message;
        this.email = email;
        this.remainingAttempts = remainingAttempts;
        this.minutesUntilExpiry = minutesUntilExpiry;
        this.hasActiveCode = hasActiveCode;
    }
    
    // Métodos estáticos para respuestas comunes
    public static EmailVerificationResponse success(String message) {
        return new EmailVerificationResponse(true, message);
    }
    
    public static EmailVerificationResponse error(String message) {
        return new EmailVerificationResponse(false, message);
    }
    
    public static EmailVerificationResponse codeSent(String email, int remainingAttempts, long minutesUntilExpiry) {
        EmailVerificationResponse response = new EmailVerificationResponse(true, "Código de verificación enviado exitosamente");
        response.setEmail(email);
        response.setRemainingAttempts(remainingAttempts);
        response.setMinutesUntilExpiry(minutesUntilExpiry);
        response.setHasActiveCode(true);
        return response;
    }
    
    public static EmailVerificationResponse verified(String email) {
        EmailVerificationResponse response = new EmailVerificationResponse(true, "Email verificado exitosamente");
        response.setEmail(email);
        response.setHasActiveCode(false);
        return response;
    }
    
    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getRemainingAttempts() {
        return remainingAttempts;
    }
    
    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
    
    public long getMinutesUntilExpiry() {
        return minutesUntilExpiry;
    }
    
    public void setMinutesUntilExpiry(long minutesUntilExpiry) {
        this.minutesUntilExpiry = minutesUntilExpiry;
    }
    
    public boolean isHasActiveCode() {
        return hasActiveCode;
    }
    
    public void setHasActiveCode(boolean hasActiveCode) {
        this.hasActiveCode = hasActiveCode;
    }
    
    @Override
    public String toString() {
        return "EmailVerificationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", email='" + email + '\'' +
                ", remainingAttempts=" + remainingAttempts +
                ", minutesUntilExpiry=" + minutesUntilExpiry +
                ", hasActiveCode=" + hasActiveCode +
                '}';
    }
}

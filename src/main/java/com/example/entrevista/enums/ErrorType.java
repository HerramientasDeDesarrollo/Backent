package com.example.entrevista.enums;

public enum ErrorType {
    // Errores de validación
    VALIDATION_ERROR("VALIDATION_ERROR", "Error de validación de datos"),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "Email ya registrado"),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "Formato de email inválido"),
    INVALID_PHONE_FORMAT("INVALID_PHONE_FORMAT", "Formato de teléfono inválido"),
    INVALID_NAME_FORMAT("INVALID_NAME_FORMAT", "Formato de nombre inválido"),
    
    // Errores de autenticación
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Credenciales incorrectas"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token expirado"),
    TOKEN_INVALID("TOKEN_INVALID", "Token inválido"),
    UNAUTHORIZED("UNAUTHORIZED", "No autorizado"),
    
    // Errores de verificación de email
    VERIFICATION_CODE_INVALID("VERIFICATION_CODE_INVALID", "Código de verificación inválido"),
    VERIFICATION_CODE_EXPIRED("VERIFICATION_CODE_EXPIRED", "Código de verificación expirado"),
    VERIFICATION_LIMIT_EXCEEDED("VERIFICATION_LIMIT_EXCEEDED", "Límite de códigos excedido"),
    EMAIL_ALREADY_VERIFIED("EMAIL_ALREADY_VERIFIED", "Email ya verificado"),
    EMAIL_NOT_REGISTERED("EMAIL_NOT_REGISTERED", "Email no registrado"),
    
    // Errores de recursos
    USER_NOT_FOUND("USER_NOT_FOUND", "Usuario no encontrado"),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND", "Empresa no encontrada"),
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND", "Perfil no encontrado"),
    CONVOCATORIA_NOT_FOUND("CONVOCATORIA_NOT_FOUND", "Convocatoria no encontrada"),
    
    // Errores de archivo
    FILE_EMPTY("FILE_EMPTY", "Archivo vacío"),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "Archivo muy grande"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Tipo de archivo no válido"),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR", "Error al subir archivo"),
    
    // Errores de negocio
    ALREADY_APPLIED("ALREADY_APPLIED", "Ya se postuló a esta convocatoria"),
    CONVOCATORIA_INACTIVE("CONVOCATORIA_INACTIVE", "Convocatoria inactiva"),
    INTERVIEW_ALREADY_COMPLETED("INTERVIEW_ALREADY_COMPLETED", "Entrevista ya completada"),
    
    // Errores del servidor
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Error interno del servidor"),
    DATABASE_ERROR("DATABASE_ERROR", "Error de base de datos"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "Error de servicio externo"),
    
    // Errores generales
    INVALID_REQUEST("INVALID_REQUEST", "Solicitud inválida"),
    MISSING_PARAMETER("MISSING_PARAMETER", "Parámetro faltante"),
    INVALID_PARAMETER("INVALID_PARAMETER", "Parámetro inválido");
    
    private final String code;
    private final String description;
    
    ErrorType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}

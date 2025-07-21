package com.example.entrevista.exception;

import com.example.entrevista.DTO.ApiResponse;
import com.example.entrevista.enums.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Errores de validación: {}", errors);
        
        ApiResponse<Object> response = ApiResponse.validationError(errors, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.badRequest(
            ex.getMessage(), 
            ErrorType.INVALID_PARAMETER
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex) {
        
        logger.error("Error de runtime: {}", ex.getMessage());
        
        // Determinar tipo de error basado en el mensaje
        ErrorType errorType = determineErrorType(ex.getMessage());
        HttpStatus status = getStatusForErrorType(errorType);
        
        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(), 
            errorType, 
            status
        );
        
        return ResponseEntity.status(status).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex) {
        
        logger.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.internalError(
            "Error interno del servidor"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private ErrorType determineErrorType(String message) {
        if (message.contains("ya está registrado")) {
            return ErrorType.DUPLICATE_EMAIL;
        } else if (message.contains("no encontrado")) {
            return ErrorType.USER_NOT_FOUND;
        } else if (message.contains("archivo")) {
            return ErrorType.FILE_UPLOAD_ERROR;
        } else if (message.contains("código")) {
            return ErrorType.VERIFICATION_CODE_INVALID;
        }
        return ErrorType.INTERNAL_SERVER_ERROR;
    }
    
    private HttpStatus getStatusForErrorType(ErrorType errorType) {
        switch (errorType) {
            case DUPLICATE_EMAIL:
            case VALIDATION_ERROR:
            case INVALID_PARAMETER:
                return HttpStatus.BAD_REQUEST;
            case USER_NOT_FOUND:
            case COMPANY_NOT_FOUND:
            case PROFILE_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED:
            case AUTHENTICATION_FAILED:
                return HttpStatus.UNAUTHORIZED;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}

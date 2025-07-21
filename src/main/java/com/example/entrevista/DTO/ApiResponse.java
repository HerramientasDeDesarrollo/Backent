package com.example.entrevista.DTO;

import com.example.entrevista.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private int status;              // Código HTTP (200, 400, 401, 404, etc.)
    private String message;
    private String errorType;        // Tipo específico de error para el frontend
    private String errorCode;        // Código interno de error
    private T data;                  // Datos de respuesta (puede ser null en errores)
    private Map<String, String> validationErrors; // Errores de validación específicos
    private LocalDateTime timestamp;
    
    // Constructor para respuestas exitosas
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Operación exitosa");
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Constructor para respuestas exitosas con mensaje personalizado
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Constructor para respuestas exitosas con status personalizado
    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(status.value());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Constructor para errores generales
    public static <T> ApiResponse<T> error(String message, ErrorType errorType, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setStatus(status.value());
        response.setMessage(message);
        response.setErrorType(errorType.getCode());
        response.setErrorCode(errorType.getCode());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Constructor para errores con validaciones
    public static <T> ApiResponse<T> validationError(Map<String, String> validationErrors, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setStatus(status.value());
        response.setMessage("Errores de validación");
        response.setErrorType(ErrorType.VALIDATION_ERROR.getCode());
        response.setErrorCode(ErrorType.VALIDATION_ERROR.getCode());
        response.setValidationErrors(validationErrors);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Constructor para error 400 Bad Request
    public static <T> ApiResponse<T> badRequest(String message, ErrorType errorType) {
        return error(message, errorType, HttpStatus.BAD_REQUEST);
    }
    
    // Constructor para error 401 Unauthorized
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, ErrorType.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }
    
    // Constructor para error 404 Not Found
    public static <T> ApiResponse<T> notFound(String message, ErrorType errorType) {
        return error(message, errorType, HttpStatus.NOT_FOUND);
    }
    
    // Constructor para error 500 Internal Server Error
    public static <T> ApiResponse<T> internalError(String message) {
        return error(message, ErrorType.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Constructor para crear (201 Created)
    public static <T> ApiResponse<T> created(T data, String message) {
        return success(data, message, HttpStatus.CREATED);
    }
}

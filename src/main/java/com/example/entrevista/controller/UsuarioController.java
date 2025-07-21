package com.example.entrevista.controller;

import com.example.entrevista.DTO.UsuarioCreateDTO;
import com.example.entrevista.DTO.UsuarioResponseDTO;
import com.example.entrevista.DTO.ApiResponse;
import com.example.entrevista.enums.ErrorType;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.service.UsuarioService;
import com.example.entrevista.service.EmailVerificationService;
import com.example.entrevista.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private EmailService emailService;

    // Permitir creación de usuarios (registro público) + Verificación automática
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> crear(@Valid @RequestBody UsuarioCreateDTO usuarioCreateDTO, 
                                   HttpServletRequest request) {
        try {
            logger.info("Iniciando registro de usuario: {}", usuarioCreateDTO.getEmail());
            
            // Convertir DTO a entidad
            Usuario usuario = new Usuario();
            usuario.setEmail(usuarioCreateDTO.getEmail());
            usuario.setNombre(usuarioCreateDTO.getNombre());
            usuario.setApellidoPaterno(usuarioCreateDTO.getApellidoPaterno());
            usuario.setApellidoMaterno(usuarioCreateDTO.getApellidoMaterno());
            usuario.setNacimiento(usuarioCreateDTO.getNacimiento());
            usuario.setTelefono(usuarioCreateDTO.getTelefono());
            usuario.setPassword(usuarioCreateDTO.getPassword());
            usuario.setRol(usuarioCreateDTO.getRol());
            
            // Crear usuario en base de datos
            Usuario usuarioCreado = usuarioService.crearUsuario(usuario);
            logger.info("Usuario creado exitosamente: {}", usuarioCreado.getEmail());
            
            // ENVÍO AUTOMÁTICO DE CÓDIGO DE VERIFICACIÓN
            try {
                String clientIp = getClientIpAddress(request);
                // Usar método actualizado que busca automáticamente el usuario
                var verification = emailVerificationService.createVerificationCodeForUsuario(
                    usuarioCreado, 
                    clientIp
                );
                
                // Enviar email con código (ORDEN CORREGIDO)
                emailService.sendVerificationEmail(
                    verification.getEmail(),           // toEmail
                    verification.getVerificationCode(), // verificationCode  
                    verification.getDisplayName(),     // userName
                    verification.getUserType()         // userType
                );
                
                logger.info("Código de verificación enviado automáticamente a: {}", usuarioCreado.getEmail());
                
            } catch (Exception emailError) {
                logger.warn("Error al enviar código de verificación (usuario creado correctamente): {}", emailError.getMessage());
            }
            
            // Convertir entidad a DTO de respuesta
            UsuarioResponseDTO response = new UsuarioResponseDTO();
            response.setId(usuarioCreado.getId());
            response.setEmail(usuarioCreado.getEmail());
            response.setNombre(usuarioCreado.getNombre());
            response.setApellidoPaterno(usuarioCreado.getApellidoPaterno());
            response.setApellidoMaterno(usuarioCreado.getApellidoMaterno());
            response.setNacimiento(usuarioCreado.getNacimiento());
            response.setTelefono(usuarioCreado.getTelefono());
            response.setRol(usuarioCreado.getRol());
            
            // Crear datos de respuesta
            Map<String, Object> responseData = Map.of(
                "user", response,
                "requiresEmailVerification", true,
                "email", usuarioCreado.getEmail()
            );
            
            // Respuesta exitosa usando ApiResponse
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.created(
                responseData, 
                "Usuario registrado exitosamente. Se ha enviado un código de verificación a tu email."
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error al crear usuario: {}", e.getMessage());
            
            // Verificar si es error de email duplicado
            if (e.getMessage().contains("ya está registrado")) {
                ApiResponse<Map<String, Object>> apiResponse = ApiResponse.badRequest(
                    e.getMessage(), 
                    ErrorType.DUPLICATE_EMAIL
                );
                return ResponseEntity.badRequest().body(apiResponse);
            }
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.badRequest(
                "Error al crear usuario: " + e.getMessage(),
                ErrorType.INVALID_REQUEST
            );
            return ResponseEntity.badRequest().body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Error interno al crear usuario: {}", e.getMessage());
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.internalError(
                "Error interno del servidor"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // Solo usuarios pueden ver su propio perfil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    UsuarioResponseDTO response = new UsuarioResponseDTO();
                    response.setId(usuario.getId());
                    response.setEmail(usuario.getEmail());
                    response.setNombre(usuario.getNombre());
                    response.setApellidoPaterno(usuario.getApellidoPaterno());
                    response.setApellidoMaterno(usuario.getApellidoMaterno());
                    response.setNacimiento(usuario.getNacimiento());
                    response.setTelefono(usuario.getTelefono());
                    response.setRol(usuario.getRol());
                    
                    ApiResponse<UsuarioResponseDTO> apiResponse = ApiResponse.success(
                        response, 
                        "Usuario encontrado"
                    );
                    return ResponseEntity.ok(apiResponse);
                })
                .orElseGet(() -> {
                    ApiResponse<UsuarioResponseDTO> apiResponse = ApiResponse.notFound(
                        "Usuario no encontrado", 
                        ErrorType.USER_NOT_FOUND
                    );
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
                });
    }

    // Para autenticación - permitir búsqueda por email (mantener Usuario para autenticación)
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Solo admins pueden listar todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioResponseDTO> response = usuarios.stream()
                .map(usuario -> {
                    UsuarioResponseDTO dto = new UsuarioResponseDTO();
                    dto.setId(usuario.getId());
                    dto.setEmail(usuario.getEmail());
                    dto.setNombre(usuario.getNombre());
                    dto.setApellidoPaterno(usuario.getApellidoPaterno());
                    dto.setApellidoMaterno(usuario.getApellidoMaterno());
                    dto.setNacimiento(usuario.getNacimiento());
                    dto.setTelefono(usuario.getTelefono());
                    dto.setRol(usuario.getRol());
                    return dto;
                })
                .collect(Collectors.toList());
        
        ApiResponse<List<UsuarioResponseDTO>> apiResponse = ApiResponse.success(
            response,
            "Usuarios obtenidos exitosamente"
        );
        return ResponseEntity.ok(apiResponse);
    }
}

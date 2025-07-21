package com.example.entrevista.controller;

import com.example.entrevista.DTO.AuthRequest;
import com.example.entrevista.DTO.AuthResponse;
import com.example.entrevista.DTO.EmailVerificationRequest;
import com.example.entrevista.DTO.VerifyCodeRequest;
import com.example.entrevista.DTO.EmailStatusRequest;
import com.example.entrevista.DTO.EmailVerificationResponse;
import com.example.entrevista.util.JwtUtil;
import com.example.entrevista.service.CustomUserDetailsService;
import com.example.entrevista.service.EmailVerificationService;
import com.example.entrevista.service.EmailService;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.model.EmailVerification;
import com.example.entrevista.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo y contraseña son obligatorios"));
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            System.out.println("userDetails: " + userDetails);
            
            // Get the full authority including "ROLE_" prefix
            String authority = userDetails.getAuthorities().iterator().next().getAuthority();
            
            // Obtener información adicional del usuario o empresa
            Usuario usuario = userDetailsService.findUsuarioByEmail(request.getEmail());
            Empresa empresa = null;
            
            String token;
            AuthResponse response;
            
            if (usuario != null) {
                // Es un usuario
                token = jwtUtil.generateToken(
                    userDetails.getUsername(), 
                    authority,
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno()
                );
                response = new AuthResponse(
                    token,
                    "USUARIO",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno(),
                    usuario.getEmail()
                );
            } else {
                // Es una empresa
                empresa = userDetailsService.findEmpresaByEmail(request.getEmail());
                if (empresa != null) {
                    token = jwtUtil.generateToken(
                        userDetails.getUsername(), 
                        authority,
                        empresa.getId(),
                        empresa.getNombre(),
                        null, // Las empresas no tienen apellidos
                        null
                    );
                    response = new AuthResponse(
                        token,
                        "EMPRESA",
                        empresa.getId(),
                        empresa.getNombre(),
                        null, // Las empresas no tienen apellidos
                        null,
                        empresa.getEmail()
                    );
                } else {
                    // Fallback al método original
                    token = jwtUtil.generateToken(userDetails.getUsername(), authority);
                    response = new AuthResponse(token);
                }
            }
            
            System.out.println("token generado: " + token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            System.out.println("Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                
                Map<String, Object> userInfo = Map.of(
                    "id", userPrincipal.getId(),
                    "email", userPrincipal.getEmail(),
                    "nombre", userPrincipal.getNombre() != null ? userPrincipal.getNombre() : "",
                    "apellidoPaterno", userPrincipal.getApellidoPaterno() != null ? userPrincipal.getApellidoPaterno() : "",
                    "apellidoMaterno", userPrincipal.getApellidoMaterno() != null ? userPrincipal.getApellidoMaterno() : "",
                    "nombreCompleto", userPrincipal.getNombreCompleto(),
                    "userType", userPrincipal.getUserType(),
                    "roles", authentication.getAuthorities()
                );
                
                return ResponseEntity.ok(userInfo);
            }
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    // =============================================================================
    // ENDPOINTS DE VERIFICACIÓN DE EMAIL
    // =============================================================================
    
    /**
     * Envía un código de verificación al email especificado
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<EmailVerificationResponse> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Solicitud de código de verificación para: {} tipo: {}", request.getEmail(), request.getUserType());
        
        try {
            // VALIDAR SI EL EMAIL YA ESTÁ REGISTRADO
            boolean isRegisteredUser = userDetailsService.findUsuarioByEmail(request.getEmail()) != null;
            boolean isRegisteredEmpresa = userDetailsService.findEmpresaByEmail(request.getEmail()) != null;
            
            if (isRegisteredUser || isRegisteredEmpresa) {
                logger.warn("Intento de envío de código a email ya registrado: {}", request.getEmail());
                return ResponseEntity.badRequest().body(
                    EmailVerificationResponse.error("Este email ya está registrado. Usa la opción de login para acceder a tu cuenta.")
                );
            }
            
            // Verificar si el usuario puede solicitar un nuevo código
            if (!emailVerificationService.canRequestNewCode(request.getEmail())) {
                int remaining = emailVerificationService.getRemainingAttempts(request.getEmail());
                return ResponseEntity.badRequest().body(
                    EmailVerificationResponse.error("Has alcanzado el límite de códigos por día. Intentos restantes: " + remaining)
                );
            }
            
            // Verificar si ya tiene un código activo
            if (emailVerificationService.hasActiveCode(request.getEmail())) {
                long minutesLeft = emailVerificationService.getMinutesUntilExpiry(request.getEmail());
                return ResponseEntity.badRequest().body(
                    EmailVerificationResponse.error("Ya tienes un código activo. Expira en " + minutesLeft + " minutos.")
                );
            }
            
            // NOTA: No se puede crear código para emails no registrados aún
            // Este endpoint debe usarse solo DESPUÉS del registro
            logger.info("Email {} no está registrado aún. Debe registrarse primero.", request.getEmail());
            return ResponseEntity.badRequest().body(
                EmailVerificationResponse.error("Email no encontrado. Debes registrarse primero antes de solicitar verificación.")
            );
            
        } catch (Exception e) {
            logger.error("Error al enviar código de verificación a {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500).body(
                EmailVerificationResponse.error("Error interno del servidor: " + e.getMessage())
            );
        }
    }
    
    /**
     * Valida un código de verificación
     */
    @PostMapping("/verify-code")
    public ResponseEntity<EmailVerificationResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        
        logger.info("Validando código de verificación para: {}", request.getEmail());
        
        try {
            boolean isValid = emailVerificationService.validateVerificationCode(
                request.getEmail(), 
                request.getVerificationCode()
            );
            
            if (isValid) {
                logger.info("Código de verificación válido para: {}", request.getEmail());
                return ResponseEntity.ok(
                    EmailVerificationResponse.verified(request.getEmail())
                );
            } else {
                logger.warn("Código de verificación inválido para: {}", request.getEmail());
                return ResponseEntity.badRequest().body(
                    EmailVerificationResponse.error("Código de verificación inválido o expirado")
                );
            }
            
        } catch (Exception e) {
            logger.error("Error al validar código para {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500).body(
                EmailVerificationResponse.error("Error interno del servidor: " + e.getMessage())
            );
        }
    }
    
    /**
     * Reenvía un código de verificación
     */
    @PostMapping("/resend-verification-code")
    public ResponseEntity<EmailVerificationResponse> resendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Reenviando código de verificación para: {}", request.getEmail());
        
        try {
            // Verificar límites
            if (!emailVerificationService.canRequestNewCode(request.getEmail())) {
                int remaining = emailVerificationService.getRemainingAttempts(request.getEmail());
                return ResponseEntity.badRequest().body(
                    EmailVerificationResponse.error("Has alcanzado el límite de códigos por día. Intentos restantes: " + remaining)
                );
            }
            
            // Obtener IP del cliente
            String clientIp = getClientIpAddress(httpRequest);
            
            // Crear nuevo código (método actualizado)
            EmailVerification verification = emailVerificationService.createVerificationCode(
                request.getEmail(), 
                clientIp
            );
            
            // Enviar email (ORDEN CORREGIDO)
            emailService.sendVerificationEmail(
                verification.getEmail(),           // toEmail
                verification.getVerificationCode(), // verificationCode
                verification.getDisplayName(),     // userName
                verification.getUserType()         // userType
            );
            
            // Respuesta exitosa
            int remaining = emailVerificationService.getRemainingAttempts(request.getEmail());
            long minutesUntilExpiry = emailVerificationService.getMinutesUntilExpiry(request.getEmail());
            
            logger.info("Código de verificación reenviado exitosamente a: {}", request.getEmail());
            
            return ResponseEntity.ok(
                EmailVerificationResponse.codeSent(request.getEmail(), remaining, minutesUntilExpiry)
            );
            
        } catch (Exception e) {
            logger.error("Error al reenviar código a {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500).body(
                EmailVerificationResponse.error("Error interno del servidor: " + e.getMessage())
            );
        }
    }
    
    /**
     * Verifica el estado de verificación de un email (MÉTODO SEGURO)
     * Solo devuelve información si el email existe y tiene código activo
     */
    @PostMapping("/verification-status")
    public ResponseEntity<EmailVerificationResponse> getVerificationStatus(@RequestBody EmailStatusRequest request) {
        
        logger.info("Consultando estado de verificación (método seguro)");
        
        try {
            // Verificar si el email existe en el sistema (sin exponerlo)
            if (!emailVerificationService.hasActiveCode(request.getEmail())) {
                // No revelar si el email existe o no
                return ResponseEntity.ok(new EmailVerificationResponse(
                    false, 
                    "Si el email está registrado, recibirá las instrucciones correspondientes", 
                    null, // No devolver el email
                    0, 
                    0,
                    false
                ));
            }
            
            boolean hasActiveCode = emailVerificationService.hasActiveCode(request.getEmail());
            int remainingAttempts = emailVerificationService.getRemainingAttempts(request.getEmail());
            long minutesUntilExpiry = emailVerificationService.getMinutesUntilExpiry(request.getEmail());
            
            EmailVerificationResponse response = new EmailVerificationResponse(
                true, 
                "Estado de verificación consultado exitosamente", 
                null, // No devolver el email por seguridad
                remainingAttempts, 
                minutesUntilExpiry, 
                hasActiveCode
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al consultar estado: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                EmailVerificationResponse.error("Error interno del servidor: " + e.getMessage())
            );
        }
    }
    
    // =============================================================================
    // MÉTODOS UTILITARIOS
    // =============================================================================
    
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
}

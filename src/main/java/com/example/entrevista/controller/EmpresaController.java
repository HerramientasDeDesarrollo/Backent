package com.example.entrevista.controller;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.service.EmpresaService;
import com.example.entrevista.service.EmailVerificationService;
import com.example.entrevista.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);

    @Autowired
    private EmpresaService empresaService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private EmailService emailService;
    
    // Permitir creación de empresas (registro público) + Verificación automática
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Empresa empresa, HttpServletRequest request) {
        try {
            logger.info("Iniciando registro de empresa: {}", empresa.getEmail());
            
            // Crear empresa en base de datos
            Empresa empresaCreada = empresaService.crearEmpresa(empresa);
            logger.info("Empresa creada exitosamente: {}", empresaCreada.getEmail());
            
            // ENVÍO AUTOMÁTICO DE CÓDIGO DE VERIFICACIÓN
            try {
                String clientIp = getClientIpAddress(request);
                // Usar método actualizado que trabaja directamente con la empresa
                var verification = emailVerificationService.createVerificationCodeForEmpresa(
                    empresaCreada, 
                    clientIp
                );
                
                // Enviar email con código (ORDEN CORREGIDO)
                emailService.sendVerificationEmail(
                    verification.getEmail(),           // toEmail
                    verification.getVerificationCode(), // verificationCode
                    verification.getDisplayName(),     // userName  
                    verification.getUserType()         // userType
                );
                
                logger.info("Código de verificación enviado automáticamente a empresa: {}", empresaCreada.getEmail());
                
            } catch (Exception emailError) {
                logger.warn("Error al enviar código de verificación (empresa creada correctamente): {}", emailError.getMessage());
            }
            
            // Respuesta con información de verificación
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Empresa registrada exitosamente. Se ha enviado un código de verificación a tu email.",
                "empresa", empresaCreada,
                "requiresEmailVerification", true,
                "email", empresaCreada.getEmail()
            ));
            
        } catch (RuntimeException e) {
            logger.error("Error al crear empresa: {}", e.getMessage());
            
            // Verificar si es error de email duplicado
            if (e.getMessage().contains("ya está registrado")) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "errorType", "DUPLICATE_EMAIL"
                ));
            }
            
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "error", "Error al crear empresa: " + e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error al crear empresa: {}", e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of(
                "success", false,
                "error", "Error al crear empresa: " + e.getMessage()
            ));
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

    // Solo empresas pueden ver su propio perfil, usuarios pueden ver info básica
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('USUARIO')")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        return empresaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Para autenticación - permitir búsqueda por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Empresa> buscarPorEmail(@PathVariable String email) {
        return empresaService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Solo admins pueden listar todas las empresas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Empresa>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }
}

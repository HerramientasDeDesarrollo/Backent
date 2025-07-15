package com.example.entrevista.service;

import com.example.entrevista.model.EmailVerification;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.repository.EmailVerificationRepository;
import com.example.entrevista.repository.UsuarioRepository;
import com.example.entrevista.repository.EmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class EmailVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Value("${app.email.verification.expiry-minutes:15}")
    private int expiryMinutes;
    
    @Value("${app.email.verification.max-attempts-per-day:3}")
    private int maxAttemptsPerDay;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Genera un código de verificación seguro de 6 dígitos
     */
    public String generateVerificationCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Crea un nuevo código de verificación para un usuario
     */
    public EmailVerification createVerificationCodeForUsuario(Usuario usuario, String ipAddress) {
        logger.info("Creando código de verificación para usuario: {}", usuario.getEmail());
        
        // Verificar límite de intentos
        if (!canRequestNewCode(usuario.getEmail())) {
            throw new RuntimeException("Has alcanzado el límite máximo de códigos por día");
        }
        
        // Invalidar códigos anteriores del mismo email
        emailVerificationRepository.invalidatePreviousCodesByEmail(usuario.getEmail(), LocalDateTime.now());
        
        // Crear nuevo código usando el método helper del repository
        EmailVerification verification = emailVerificationRepository.createForUsuario(
            usuario, 
            generateVerificationCode(), 
            LocalDateTime.now(), 
            LocalDateTime.now().plusMinutes(expiryMinutes)
        );
        verification.setIpAddress(ipAddress);
        
        EmailVerification saved = emailVerificationRepository.save(verification);
        logger.info("Código de verificación creado para usuario: {}", saved.getVerificationCode());
        
        return saved;
    }
    
    /**
     * Crea un nuevo código de verificación para una empresa
     */
    public EmailVerification createVerificationCodeForEmpresa(Empresa empresa, String ipAddress) {
        logger.info("Creando código de verificación para empresa: {}", empresa.getEmail());
        
        // Verificar límite de intentos
        if (!canRequestNewCode(empresa.getEmail())) {
            throw new RuntimeException("Has alcanzado el límite máximo de códigos por día");
        }
        
        // Invalidar códigos anteriores del mismo email
        emailVerificationRepository.invalidatePreviousCodesByEmail(empresa.getEmail(), LocalDateTime.now());
        
        // Crear nuevo código usando el método helper del repository
        EmailVerification verification = emailVerificationRepository.createForEmpresa(
            empresa, 
            generateVerificationCode(), 
            LocalDateTime.now(), 
            LocalDateTime.now().plusMinutes(expiryMinutes)
        );
        verification.setIpAddress(ipAddress);
        
        EmailVerification saved = emailVerificationRepository.save(verification);
        logger.info("Código de verificación creado para empresa: {}", saved.getVerificationCode());
        
        return saved;
    }
    
    /**
     * Método helper que crea código de verificación por email (busca automáticamente)
     */
    public EmailVerification createVerificationCode(String email, String ipAddress) {
        logger.info("Creando código de verificación para email: {}", email);
        
        // Buscar si es usuario
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isPresent()) {
            return createVerificationCodeForUsuario(usuario.get(), ipAddress);
        }
        
        // Buscar si es empresa
        Optional<Empresa> empresa = empresaRepository.findByEmail(email);
        if (empresa.isPresent()) {
            return createVerificationCodeForEmpresa(empresa.get(), ipAddress);
        }
        
        throw new RuntimeException("No se encontró usuario o empresa con el email: " + email);
    }
    
    /**
     * Valida un código de verificación
     */
    public boolean validateVerificationCode(String email, String code) {
        logger.info("Validando código de verificación para email: {}", email);
        
        // Primero intenta buscar en usuarios
        Optional<EmailVerification> verification = emailVerificationRepository
            .findByUsuarioEmailAndVerificationCodeAndIsUsedFalseAndExpiresAtAfter(
                email, code, LocalDateTime.now()
            );
        
        // Si no se encuentra en usuarios, busca en empresas
        if (!verification.isPresent()) {
            verification = emailVerificationRepository
                .findByEmpresaEmailAndVerificationCodeAndIsUsedFalseAndExpiresAtAfter(
                    email, code, LocalDateTime.now()
                );
        }
        
        if (verification.isPresent()) {
            // Marcar como usado
            EmailVerification ver = verification.get();
            ver.setUsed(true);
            ver.setUsedAt(LocalDateTime.now());
            emailVerificationRepository.save(ver);
            
            logger.info("Código de verificación válido y marcado como usado para: {}", email);
            return true;
        }
        
        logger.warn("Código de verificación inválido o expirado para email: {}", email);
        return false;
    }
    
    /**
     * Verifica si se puede solicitar un nuevo código
     */
    public boolean canRequestNewCode(String email) {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long attempts = emailVerificationRepository.countByEmailAndCreatedAtAfter(email, last24Hours);
        
        boolean canRequest = attempts < maxAttemptsPerDay;
        logger.info("Verificando límite para {}: {} intentos de {} permitidos", email, attempts, maxAttemptsPerDay);
        
        return canRequest;
    }
    
    /**
     * Obtiene información del último código para un email
     */
    public Optional<EmailVerification> getActiveVerificationCode(String email) {
        return emailVerificationRepository
            .findByUserEmailOrEmpresaEmailAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                email, LocalDateTime.now()
            );
    }
    
    /**
     * Obtiene el tiempo restante hasta la expiración en minutos
     */
    public long getMinutesUntilExpiry(String email) {
        Optional<EmailVerification> verification = getActiveVerificationCode(email);
        
        if (verification.isPresent()) {
            return verification.get().getMinutesUntilExpiry();
        }
        
        return 0;
    }
    
    /**
     * Verifica si existe un código activo para un email
     */
    public boolean hasActiveCode(String email) {
        return emailVerificationRepository.existsByUserEmailOrEmpresaEmailAndIsUsedFalseAndExpiresAtAfter(
            email, LocalDateTime.now()
        );
    }
    
    /**
     * Obtiene el número de intentos restantes para un email
     */
    public int getRemainingAttempts(String email) {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long attempts = emailVerificationRepository.countByEmailAndCreatedAtAfter(email, last24Hours);
        
        return Math.max(0, maxAttemptsPerDay - (int) attempts);
    }
    
    /**
     * Limpia códigos expirados (para tarea programada)
     */
    public int cleanupExpiredCodes() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int deleted = emailVerificationRepository.deleteExpiredCodes(cutoff);
        
        if (deleted > 0) {
            logger.info("Limpieza automática: {} códigos expirados eliminados", deleted);
        }
        
        return deleted;
    }
}

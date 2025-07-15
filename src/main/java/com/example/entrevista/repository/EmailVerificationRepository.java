package com.example.entrevista.repository;

import com.example.entrevista.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    // ✅ BUSCAR POR RELACIÓN DE USUARIO
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.usuario.email = :email AND ev.verificationCode = :code AND ev.isUsed = false AND ev.expiresAt > :now")
    Optional<EmailVerification> findByUsuarioEmailAndVerificationCodeAndIsUsedFalseAndExpiresAtAfter(
        @Param("email") String email, @Param("code") String code, @Param("now") LocalDateTime now);
    
    // ✅ BUSCAR POR RELACIÓN DE EMPRESA
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.empresa.email = :email AND ev.verificationCode = :code AND ev.isUsed = false AND ev.expiresAt > :now")
    Optional<EmailVerification> findByEmpresaEmailAndVerificationCodeAndIsUsedFalseAndExpiresAtAfter(
        @Param("email") String email, @Param("code") String code, @Param("now") LocalDateTime now);
    
    // ✅ VERIFICAR CÓDIGOS ACTIVOS (USUARIO O EMPRESA)
    @Query("SELECT COUNT(ev) > 0 FROM EmailVerification ev WHERE (ev.usuario.email = :email OR ev.empresa.email = :email) AND ev.isUsed = false AND ev.expiresAt > :now")
    boolean existsByUserEmailOrEmpresaEmailAndIsUsedFalseAndExpiresAtAfter(
        @Param("email") String email, @Param("now") LocalDateTime now);
    
    // ✅ BUSCAR ÚLTIMO CÓDIGO VÁLIDO (USUARIO O EMPRESA)
    @Query("SELECT ev FROM EmailVerification ev WHERE (ev.usuario.email = :email OR ev.empresa.email = :email) AND ev.isUsed = false AND ev.expiresAt > :now ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findByUserEmailOrEmpresaEmailAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
        @Param("email") String email, @Param("now") LocalDateTime now);
    
    // ✅ INVALIDAR CÓDIGOS ANTERIORES (USUARIO O EMPRESA)
    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.isUsed = true, ev.usedAt = :usedAt WHERE (ev.usuario.email = :email OR ev.empresa.email = :email) AND ev.isUsed = false")
    void invalidatePreviousCodesByEmail(@Param("email") String email, @Param("usedAt") LocalDateTime usedAt);
    
    // ✅ LIMPIAR CÓDIGOS EXPIRADOS (tarea programada)
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :cutoff")
    int deleteExpiredCodes(@Param("cutoff") LocalDateTime cutoff);
    
    // ✅ CONTAR INTENTOS POR EMAIL EN LAS ÚLTIMAS 24 HORAS (USUARIO O EMPRESA)
    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE (ev.usuario.email = :email OR ev.empresa.email = :email) AND ev.createdAt > :since")
    long countByEmailAndCreatedAtAfter(@Param("email") String email, @Param("since") LocalDateTime since);
    
    // ✅ MÉTODOS AUXILIARES PARA CREAR VERIFICACIONES
    default EmailVerification createForUsuario(com.example.entrevista.model.Usuario usuario, String code, LocalDateTime createdAt, LocalDateTime expiresAt) {
        EmailVerification verification = new EmailVerification();
        verification.setUsuario(usuario);
        verification.setVerificationCode(code);
        verification.setCreatedAt(createdAt);
        verification.setExpiresAt(expiresAt);
        verification.setUsed(false);
        verification.setAttemptsCount(0);
        return verification;
    }
    
    default EmailVerification createForEmpresa(com.example.entrevista.model.Empresa empresa, String code, LocalDateTime createdAt, LocalDateTime expiresAt) {
        EmailVerification verification = new EmailVerification();
        verification.setEmpresa(empresa);
        verification.setVerificationCode(code);
        verification.setCreatedAt(createdAt);
        verification.setExpiresAt(expiresAt);
        verification.setUsed(false);
        verification.setAttemptsCount(0);
        return verification;
    }
    
    // ✅ BUSCAR POR EMAIL SIN IMPORTAR ESTADO (para estadísticas) - ACTUALIZADO
    @Query("SELECT ev FROM EmailVerification ev WHERE (ev.usuario.email = :email OR ev.empresa.email = :email) ORDER BY ev.createdAt DESC")
    java.util.List<EmailVerification> findByEmailOrderByCreatedAtDesc(@Param("email") String email);
}

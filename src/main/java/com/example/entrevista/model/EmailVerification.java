package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // RELACIÓN DIRECTA con Usuario (en lugar de duplicar email)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
    
    // RELACIÓN DIRECTA con Empresa (en lugar de duplicar email)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = true)
    private Empresa empresa;
    
    @Column(name = "verification_code", length = 6, nullable = false)
    private String verificationCode;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    // Eliminamos userType porque lo sacamos de la relación
    // @Column(name = "user_type", length = 20, nullable = false)
    // private String userType; // Ya no necesario
    
    @Column(name = "attempts_count", nullable = false)
    private int attemptsCount = 0;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isUsed && !isExpired();
    }
    
    public long getMinutesUntilExpiry() {
        if (isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }
    
    // ✅ MÉTODOS PARA OBTENER EMAIL Y TIPO SIN DUPLICACIÓN
    public String getEmail() {
        if (usuario != null) {
            return usuario.getEmail();
        } else if (empresa != null) {
            return empresa.getEmail();
        }
        return null;
    }
    
    public String getUserType() {
        if (usuario != null) {
            return "USUARIO";
        } else if (empresa != null) {
            return "EMPRESA";
        }
        return null;
    }
    
    // Método para obtener el nombre del usuario/empresa
    public String getDisplayName() {
        if (usuario != null) {
            return usuario.getNombre() + " " + usuario.getApellidoPaterno();
        } else if (empresa != null) {
            return empresa.getNombre();
        }
        return "Usuario desconocido";
    }
}

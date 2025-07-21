package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Perfil {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Solo ONE de estos dos será not-null
    @Column(name = "usuario_id")
    private Long usuarioId; // Referencia simple, no relación JPA
    
    @Column(name = "empresa_id")
    private Long empresaId; // Referencia simple, no relación JPA
    
    // Solo la imagen
    @Column(name = "imagen_url")
    private String imagenUrl; // "/uploads/profiles/user_123.jpg"
    
    // Fechas básicas
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Auto-completar fechas
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Métodos simples de utilidad
    public boolean esUsuario() {
        return usuarioId != null;
    }
    
    public boolean esEmpresa() {
        return empresaId != null;
    }
    
    public boolean tieneImagen() {
        return imagenUrl != null && !imagenUrl.isEmpty();
    }
}

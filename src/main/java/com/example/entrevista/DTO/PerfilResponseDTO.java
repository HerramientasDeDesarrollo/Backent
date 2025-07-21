package com.example.entrevista.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDTO {
    
    private Long id;
    private Long usuarioId;
    private Long empresaId;
    private String imagenUrl;
    private boolean tieneImagen;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Estos campos se calculan dinámicamente al crear el DTO
    private String nombreCompleto; // Se obtiene de Usuario/Empresa
    private String imagenUrlCompleta; // URL completa para el frontend
    private String tipoPerfil; // "USUARIO" o "EMPRESA"
    
    // Constructor simplificado
    public PerfilResponseDTO(Long id, Long usuarioId, Long empresaId, String imagenUrl) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.imagenUrl = imagenUrl;
        this.tieneImagen = imagenUrl != null && !imagenUrl.isEmpty();
        this.tipoPerfil = usuarioId != null ? "USUARIO" : "EMPRESA";
    }
}

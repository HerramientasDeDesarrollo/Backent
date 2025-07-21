package com.example.entrevista.controller;

import com.example.entrevista.service.PerfilService;
import com.example.entrevista.DTO.PerfilResponseDTO;
import com.example.entrevista.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    
    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);
    
    @Autowired
    private PerfilService perfilService;
    
    // Obtener mi perfil (usuario o empresa autenticado)
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> obtenerMiPerfil() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            Optional<PerfilResponseDTO> perfil;
            
            if (userPrincipal.getUserType().equals("USUARIO")) {
                perfil = perfilService.obtenerPerfilUsuario(userPrincipal.getId());
                // Si no existe, crear uno automáticamente
                if (!perfil.isPresent()) {
                    PerfilResponseDTO nuevoPerfil = perfilService.crearPerfilUsuario(userPrincipal.getId());
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "perfil", nuevoPerfil,
                        "message", "Perfil creado automáticamente"
                    ));
                }
            } else {
                perfil = perfilService.obtenerPerfilEmpresa(userPrincipal.getId());
                // Si no existe, crear uno automáticamente
                if (!perfil.isPresent()) {
                    PerfilResponseDTO nuevoPerfil = perfilService.crearPerfilEmpresa(userPrincipal.getId());
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "perfil", nuevoPerfil,
                        "message", "Perfil creado automáticamente"
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "perfil", perfil.get()
            ));
            
        } catch (Exception e) {
            logger.error("Error al obtener perfil: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // Subir imagen de perfil
    @PostMapping("/subir-imagen")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> subirImagenPerfil(@RequestParam("imagen") MultipartFile archivo) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            // Obtener perfil existente
            Optional<PerfilResponseDTO> perfilOpt;
            if (userPrincipal.getUserType().equals("USUARIO")) {
                perfilOpt = perfilService.obtenerPerfilUsuario(userPrincipal.getId());
            } else {
                perfilOpt = perfilService.obtenerPerfilEmpresa(userPrincipal.getId());
            }
            
            if (!perfilOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Perfil no encontrado. Créalo primero."
                ));
            }
            
            PerfilResponseDTO perfilActualizado = perfilService.subirImagenPerfil(
                perfilOpt.get().getId(), 
                archivo
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "perfil", perfilActualizado,
                "message", "Imagen subida exitosamente"
            ));
            
        } catch (Exception e) {
            logger.error("Error al subir imagen: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // Eliminar imagen de perfil
    @DeleteMapping("/eliminar-imagen")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> eliminarImagenPerfil() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            // Obtener perfil existente
            Optional<PerfilResponseDTO> perfilOpt;
            if (userPrincipal.getUserType().equals("USUARIO")) {
                perfilOpt = perfilService.obtenerPerfilUsuario(userPrincipal.getId());
            } else {
                perfilOpt = perfilService.obtenerPerfilEmpresa(userPrincipal.getId());
            }
            
            if (!perfilOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Perfil no encontrado"
                ));
            }
            
            PerfilResponseDTO perfilActualizado = perfilService.eliminarImagenPerfil(perfilOpt.get().getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "perfil", perfilActualizado,
                "message", "Imagen eliminada exitosamente"
            ));
            
        } catch (Exception e) {
            logger.error("Error al eliminar imagen: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // Ver perfil público (cualquiera puede ver)
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> verPerfilUsuario(@PathVariable Long usuarioId) {
        try {
            Optional<PerfilResponseDTO> perfil = perfilService.obtenerPerfilUsuario(usuarioId);
            
            if (!perfil.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "perfil", perfil.get()
            ));
            
        } catch (Exception e) {
            logger.error("Error al obtener perfil de usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // Ver perfil público de empresa
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<?> verPerfilEmpresa(@PathVariable Long empresaId) {
        try {
            Optional<PerfilResponseDTO> perfil = perfilService.obtenerPerfilEmpresa(empresaId);
            
            if (!perfil.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "perfil", perfil.get()
            ));
            
        } catch (Exception e) {
            logger.error("Error al obtener perfil de empresa {}: {}", empresaId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

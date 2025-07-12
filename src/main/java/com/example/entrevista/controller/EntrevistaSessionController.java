package com.example.entrevista.controller;

import com.example.entrevista.service.EntrevistaSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/entrevistas")
public class EntrevistaSessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(EntrevistaSessionController.class);
    
    @Autowired
    private EntrevistaSessionService entrevistaSessionService;
    
    // Ver resultados completos por session ID
    @GetMapping("/resultados/{sessionId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> verResultadosCompletos(@PathVariable Long sessionId) {
        try {
            logger.info("Obteniendo resultados completos para sesión {}", sessionId);
            
            Map<String, Object> resultados = entrevistaSessionService.obtenerResultadosCompletos(sessionId);
            
            if (!(Boolean) resultados.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(resultados);
            }
            
            logger.info("Resultados obtenidos exitosamente para sesión {}", sessionId);
            return ResponseEntity.ok(resultados);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener resultados para sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "session_id", sessionId
                ));
        } catch (Exception e) {
            logger.error("Error interno al obtener resultados para sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "session_id", sessionId
                ));
        }
    }
    
    // Ver resumen de resultados por session ID
    @GetMapping("/resumen/{sessionId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> verResumenResultados(@PathVariable Long sessionId) {
        try {
            logger.info("Obteniendo resumen de resultados para sesión {}", sessionId);
            
            Map<String, Object> resumen = entrevistaSessionService.obtenerResumenResultados(sessionId);
            
            logger.info("Resumen obtenido exitosamente para sesión {}", sessionId);
            return ResponseEntity.ok(resumen);
            
        } catch (RuntimeException e) {
            logger.error("Error al obtener resumen para sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "session_id", sessionId
                ));
        } catch (Exception e) {
            logger.error("Error interno al obtener resumen para sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "session_id", sessionId
                ));
        }
    }
    
    // Finalizar sesión de entrevista
    @PatchMapping("/finalizar/{sessionId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> finalizarSesion(@PathVariable Long sessionId) {
        try {
            logger.info("Finalizando sesión de entrevista {}", sessionId);
            
            entrevistaSessionService.finalizarSesion(sessionId);
            
            logger.info("Sesión {} finalizada exitosamente", sessionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Sesión de entrevista finalizada exitosamente",
                "session_id", sessionId
            ));
            
        } catch (RuntimeException e) {
            logger.error("Error al finalizar sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "session_id", sessionId
                ));
        } catch (Exception e) {
            logger.error("Error interno al finalizar sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "session_id", sessionId
                ));
        }
    }
    
    // Actualizar progreso de la sesión
    @PatchMapping("/progreso/{sessionId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> actualizarProgreso(@PathVariable Long sessionId, @RequestBody Map<String, Integer> datos) {
        try {
            Integer progreso = datos.get("progreso");
            if (progreso == null || progreso < 0 || progreso > 100) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El progreso debe ser un número entre 0 y 100"));
            }
            
            logger.info("Actualizando progreso de sesión {} a {}%", sessionId, progreso);
            
            entrevistaSessionService.actualizarProgreso(sessionId, progreso);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Progreso actualizado exitosamente",
                "session_id", sessionId,
                "progreso", progreso
            ));
            
        } catch (RuntimeException e) {
            logger.error("Error al actualizar progreso de sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "session_id", sessionId
                ));
        } catch (Exception e) {
            logger.error("Error interno al actualizar progreso de sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "session_id", sessionId
                ));
        }
    }
}

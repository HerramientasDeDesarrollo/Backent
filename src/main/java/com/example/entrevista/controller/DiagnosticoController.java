package com.example.entrevista.controller;

import com.example.entrevista.service.DiagnosticoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnostico")
public class DiagnosticoController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoController.class);

    @Autowired
    private DiagnosticoService diagnosticoService;

    /**
     * Diagnóstico completo de una postulación específica
     * Accesible por usuarios para sus propias postulaciones y empresas
     */
    @GetMapping("/postulacion/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> diagnosticarPostulacion(@PathVariable Long postulacionId) {
        try {
            logger.info("Iniciando diagnóstico para postulación ID: {}", postulacionId);
            
            Map<String, Object> diagnostico = diagnosticoService.diagnosticarPostulacion(postulacionId);
            
            if (!(Boolean) diagnostico.getOrDefault("diagnostico_exitoso", false)) {
                logger.warn("Diagnóstico falló para postulación {}: {}", postulacionId, diagnostico.get("error"));
                return ResponseEntity.badRequest().body(diagnostico);
            }
            
            logger.info("Diagnóstico completado exitosamente para postulación {}", postulacionId);
            return ResponseEntity.ok(diagnostico);
            
        } catch (Exception e) {
            logger.error("Error durante diagnóstico de postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno durante el diagnóstico: " + e.getMessage(),
                    "postulacion_id", postulacionId,
                    "diagnostico_exitoso", false
                ));
        }
    }

    /**
     * Reporte general de salud del sistema
     * Solo accesible por empresas/administradores
     */
    @GetMapping("/salud-sistema")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> reporteSaludSistema() {
        try {
            logger.info("Generando reporte de salud del sistema");
            
            Map<String, Object> reporte = diagnosticoService.reporteSaludSistema();
            
            if (reporte.containsKey("error")) {
                logger.warn("Error generando reporte de salud: {}", reporte.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(reporte);
            }
            
            logger.info("Reporte de salud generado exitosamente");
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            logger.error("Error generando reporte de salud: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno generando reporte: " + e.getMessage(),
                    "timestamp", new java.util.Date()
                ));
        }
    }

    /**
     * Intenta reparar automáticamente problemas detectados
     * Solo accesible por empresas/administradores
     */
    @PostMapping("/reparar/{postulacionId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> repararPostulacion(@PathVariable Long postulacionId) {
        try {
            logger.info("Iniciando reparación automática para postulación ID: {}", postulacionId);
            
            Map<String, Object> resultado = diagnosticoService.intentarReparacionAutomatica(postulacionId);
            
            boolean exitoso = (Boolean) resultado.getOrDefault("reparacion_exitosa", false);
            
            if (exitoso) {
                logger.info("Reparación completada exitosamente para postulación {}", postulacionId);
                return ResponseEntity.ok(resultado);
            } else {
                logger.warn("Reparación falló para postulación {}: {}", postulacionId, resultado.get("errores"));
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resultado);
            }
            
        } catch (Exception e) {
            logger.error("Error durante reparación de postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno durante la reparación: " + e.getMessage(),
                    "postulacion_id", postulacionId,
                    "reparacion_exitosa", false
                ));
        }
    }

    /**
     * Endpoint de verificación rápida para una postulación
     * Retorna solo información básica sobre el estado
     */
    @GetMapping("/verificacion-rapida/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> verificacionRapida(@PathVariable Long postulacionId) {
        try {
            Map<String, Object> diagnostico = diagnosticoService.diagnosticarPostulacion(postulacionId);
            
            // Extraer solo información esencial
            Map<String, Object> verificacion = Map.of(
                "postulacion_id", postulacionId,
                "postulacion_encontrada", diagnostico.getOrDefault("postulacion_encontrada", false),
                "total_preguntas", diagnostico.getOrDefault("total_preguntas", 0),
                "total_evaluaciones", diagnostico.getOrDefault("total_evaluaciones", 0),
                "problemas_detectados", diagnostico.getOrDefault("problemas_detectados", java.util.Collections.emptyList()),
                "estado", determinarEstado(diagnostico),
                "timestamp", new java.util.Date()
            );
            
            return ResponseEntity.ok(verificacion);
            
        } catch (Exception e) {
            logger.error("Error en verificación rápida de postulación {}: {}", postulacionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error en verificación: " + e.getMessage(),
                    "postulacion_id", postulacionId,
                    "estado", "ERROR"
                ));
        }
    }

    /**
     * Endpoint para obtener métricas de calidad de datos
     */
    @GetMapping("/metricas-calidad")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> metricasCalidad() {
        try {
            Map<String, Object> reporte = diagnosticoService.reporteSaludSistema();
            
            // Extraer métricas clave
            Map<String, Object> metricas = Map.of(
                "total_postulaciones", reporte.getOrDefault("total_postulaciones", 0),
                "total_preguntas", reporte.getOrDefault("total_preguntas", 0),
                "total_evaluaciones", reporte.getOrDefault("total_evaluaciones", 0),
                "problemas_detectados", reporte.getOrDefault("total_problemas_detectados", 0),
                "porcentaje_salud", reporte.getOrDefault("porcentaje_salud", 0.0),
                "timestamp", reporte.getOrDefault("timestamp", new java.util.Date())
            );
            
            return ResponseEntity.ok(metricas);
            
        } catch (Exception e) {
            logger.error("Error obteniendo métricas de calidad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error obteniendo métricas: " + e.getMessage()));
        }
    }

    // Método auxiliar para determinar el estado general
    private String determinarEstado(Map<String, Object> diagnostico) {
        if (!(Boolean) diagnostico.getOrDefault("diagnostico_exitoso", false)) {
            return "ERROR";
        }
        
        @SuppressWarnings("unchecked")
        java.util.List<String> problemas = (java.util.List<String>) diagnostico.getOrDefault("problemas_detectados", java.util.Collections.emptyList());
        
        if (problemas == null || problemas.isEmpty()) {
            return "SALUDABLE";
        } else if (problemas.size() <= 2) {
            return "ADVERTENCIA";
        } else {
            return "CRITICO";
        }
    }
}

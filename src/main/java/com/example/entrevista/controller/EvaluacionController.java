package com.example.entrevista.controller;

import com.example.entrevista.DTO.EvaluacionRequest;
import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.service.EvaluacionService;
import com.example.entrevista.service.ResultadosService;
import com.example.entrevista.service.EntrevistaSessionService;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.model.Evaluacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private static final Logger logger = LoggerFactory.getLogger(EvaluacionController.class);

    @Autowired
    private EvaluacionService evaluacionService;
    
    @Autowired
    private ResultadosService resultadosService;
    
    @Autowired
    private EntrevistaSessionService entrevistaSessionService; // Nuevo servicio
    
    @Autowired
    private PreguntaRepository preguntaRepository;    
    
    // Solo usuarios pueden evaluar sus respuestas
    @PostMapping("/evaluar")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> evaluarRespuesta(@RequestBody Map<String, Object> requestMap) {
        logger.info("Recibida solicitud de evaluación: {}", requestMap);
        
        try {
            // Validar que se incluya preguntaId y answer
            if (!requestMap.containsKey("preguntaId")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El campo preguntaId es obligatorio"));
            }
            
            if (!requestMap.containsKey("answer")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El campo answer es obligatorio"));
            }
            
            // Convert the request map to EvaluacionRequest
            EvaluacionRequest request = convertToEvaluacionRequest(requestMap);
            
            // Validación de entrada
            Map<String, String> validationErrors = validateRequest(request);
            if (!validationErrors.isEmpty()) {
                logger.warn("Solicitud de evaluación inválida: {}", validationErrors);
                return ResponseEntity.badRequest().body(validationErrors);
            }
            
            logger.debug("Procesando solicitud: pregunta='{}', puesto='{}'", 
                    request.getQuestion(), request.getPuesto());
            
            EvaluacionResponse evaluacion = evaluacionService.evaluarPregunta(request);
            
            if (evaluacion.isSuccess()) {
                logger.info("Evaluación completada exitosamente");
            } else {
                logger.warn("La evaluación no fue exitosa");
            }
            
            return ResponseEntity.ok(evaluacion);
        } catch (IllegalArgumentException e) {
            // Errores de validación específicos
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                EvaluacionResponse.error("Error de validación: " + e.getMessage())
            );
        } catch (Exception e) {
            // Error general
            logger.error("Error al evaluar respuesta", e, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                EvaluacionResponse.error("Error al procesar la evaluación: " + e.getMessage())
            );
        }
    }
    
    private EvaluacionRequest convertToEvaluacionRequest(Map<String, Object> requestMap) {
        EvaluacionRequest request = new EvaluacionRequest();
        
        // Set answer
        if (requestMap.containsKey("answer")) {
            request.setAnswer((String) requestMap.get("answer"));
        }
        
        // Extraer preguntaId y obtener todos los datos necesarios
        Long preguntaId;
        if (requestMap.get("preguntaId") instanceof Integer) {
            preguntaId = ((Integer) requestMap.get("preguntaId")).longValue();
        } else {
            preguntaId = (Long) requestMap.get("preguntaId");
        }
        
        Optional<Pregunta> preguntaOpt = preguntaRepository.findById(preguntaId);
        if (!preguntaOpt.isPresent()) {
            throw new IllegalArgumentException("La pregunta con ID " + preguntaId + " no existe");
        }
        
        Pregunta pregunta = preguntaOpt.get();
        
        // Establecer la pregunta
        request.setQuestion(pregunta.getTextoPregunta());
        
        // Establecer el valor de la pregunta usando el score guardado en la entidad
        // Si no tiene score, se usa la dificultad de la convocatoria como valor por defecto
        int valorPregunta = pregunta.getScore() > 0 ? pregunta.getScore() : 
                           (pregunta.getConvocatoria() != null ? pregunta.getConvocatoria().getDificultad() * 10 : 10);
        request.setValorPregunta(valorPregunta);
        
        // Obtener la postulación asociada con la pregunta
        if (pregunta.getPostulacion() == null) {
            throw new IllegalArgumentException("La pregunta no tiene una postulación asociada");
        }
        
        request.setIdPostulacion(pregunta.getPostulacion().getId());
        
        // Get puesto from convocatoria if available
        if (pregunta.getConvocatoria() != null) {
            Convocatoria convocatoria = pregunta.getConvocatoria();
            request.setPuesto(convocatoria.getJobTitle());
        } else {
            throw new IllegalArgumentException("La pregunta no tiene una convocatoria con puesto especificado");
        }
        
        return request;
    }

    private Map<String, String> validateRequest(EvaluacionRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        if (request == null) {
            errors.put("request", "El cuerpo de la solicitud no puede ser nulo");
            return errors;
        }
        
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            errors.put("question", "No se pudo obtener la pregunta");
        }
        
        if (request.getAnswer() == null || request.getAnswer().trim().isEmpty()) {
            errors.put("answer", "La respuesta es obligatoria");
        }
        
        if (request.getPuesto() == null || request.getPuesto().trim().isEmpty()) {
            errors.put("puesto", "No se pudo obtener el puesto");
        }
        
        if (request.getIdPostulacion() == null) {
            errors.put("idPostulacion", "No se pudo obtener la postulación");
        }
        
        if (request.getValorPregunta() <= 0) {
            errors.put("valorPregunta", "El valor de la pregunta debe ser mayor que cero");
        }
        
        return errors;
    }    // Solo usuarios pueden ver sus propios resultados
    @GetMapping("/mis-resultados/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> verMisResultados(@PathVariable Long postulacionId) {
        try {
            logger.info("Obteniendo resultados para postulación {}", postulacionId);
            
            // Verificar primero si puede generar resultados
            if (!resultadosService.puedeGenerarResultados(postulacionId)) {
                logger.warn("La postulación {} no puede generar resultados todavía", postulacionId);
                Map<String, Object> estadisticas = resultadosService.obtenerEstadisticasRapidas(postulacionId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "mensaje", "Los datos no están completos para mostrar resultados",
                    "estadisticas", estadisticas,
                    "requiere_evaluaciones_adicionales", true
                ));
            }
            
            Map<String, Object> resultados = resultadosService.obtenerResumenResultados(postulacionId);
            
            // Verificar si hay error o datos incompletos
            if (resultados.containsKey("problemas") || 
                (resultados.containsKey("success") && !(Boolean) resultados.get("success"))) {
                logger.warn("Problemas detectados en postulación {}: {}", 
                           postulacionId, resultados.get("problemas"));
                return ResponseEntity.badRequest().body(resultados);
            }
            
            logger.info("Resultados obtenidos exitosamente para postulación {}", postulacionId);
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            logger.error("Error al obtener resultados para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "postulacion_id", postulacionId
                ));
        }
    }    
    // Solo usuarios pueden ver detalles de sus resultados
    @GetMapping("/mis-resultados/detalle/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> verMisResultadosDetalle(@PathVariable Long postulacionId) {
        try {
            logger.info("Obteniendo detalles de resultados para postulación {}", postulacionId);
            
            // Verificar primero si puede generar resultados
            if (!resultadosService.puedeGenerarResultados(postulacionId)) {
                logger.warn("La postulación {} no puede generar detalles todavía", postulacionId);
                Map<String, Object> estadisticas = resultadosService.obtenerEstadisticasRapidas(postulacionId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "mensaje", "Los datos no están completos para mostrar detalles",
                    "estadisticas", estadisticas,
                    "requiere_evaluaciones_adicionales", true
                ));
            }
            
            Map<String, Object> resultados = resultadosService.obtenerDetalleResultados(postulacionId);
            
            // Verificar si hay error o datos incompletos
            if (resultados.containsKey("problemas") || 
                (resultados.containsKey("success") && !(Boolean) resultados.get("success"))) {
                logger.warn("Problemas detectados en detalles de postulación {}: {}", 
                           postulacionId, resultados.get("problemas"));
                return ResponseEntity.badRequest().body(resultados);
            }
            
            logger.info("Detalles de resultados obtenidos exitosamente para postulación {}", postulacionId);
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            logger.error("Error al obtener detalles para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error interno al procesar la solicitud: " + e.getMessage(),
                    "postulacion_id", postulacionId
                ));
        }
    }    
    // Empresas pueden ver resultados de entrevistas de sus convocatorias
    @GetMapping("/por-entrevista/{entrevistaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> verResultadosPorEntrevista(@PathVariable Long entrevistaId) {
        try {
            Map<String, Object> resultados = resultadosService.obtenerResultadosPorEntrevista(entrevistaId);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            logger.error("Error al obtener resultados para entrevistador {}: {}", entrevistaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // Empresas pueden ver evaluaciones de una postulación específica
    @GetMapping("/postulacion/{postulacionId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> verEvaluacionesPorPostulacion(@PathVariable Long postulacionId) {
        try {
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorPostulacion(postulacionId);
            return ResponseEntity.ok(evaluaciones);
        } catch (Exception e) {
            logger.error("Error al obtener evaluaciones para postulación {}: {}", postulacionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // Endpoint para verificar el estado de una postulación antes de mostrar resultados
    @GetMapping("/verificar-estado/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> verificarEstadoPostulacion(@PathVariable Long postulacionId) {
        try {
            Map<String, Object> verificacion = resultadosService.verificarIntegridadDatos(postulacionId);
            
            // Agregar información de diagnóstico básico
            boolean valida = (Boolean) verificacion.getOrDefault("valida", false);
            
            if (!valida) {
                logger.warn("Postulación {} tiene problemas de integridad: {}", postulacionId, verificacion.get("advertencias"));
                return ResponseEntity.ok(Map.of(
                    "estado", "PROBLEMAS_DETECTADOS",
                    "puede_mostrar_resultados", false,
                    "detalles", verificacion
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "estado", "DISPONIBLE",
                "puede_mostrar_resultados", true,
                "detalles", verificacion
            ));
            
        } catch (Exception e) {
            logger.error("Error verificando estado de postulación {}: {}", postulacionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "estado", "ERROR",
                    "puede_mostrar_resultados", false,
                    "error", "Error al verificar estado: " + e.getMessage()
                ));
        }
    }
}

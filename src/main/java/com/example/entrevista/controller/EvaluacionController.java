package com.example.entrevista.controller;

import com.example.entrevista.DTO.EvaluacionRequest;
import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.service.EvaluacionService;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.model.Convocatoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluaciones")
@CrossOrigin(origins = "*")  // Enable CORS for frontend integration
public class EvaluacionController {

    private static final Logger logger = LoggerFactory.getLogger(EvaluacionController.class);

    @Autowired
    private EvaluacionService evaluacionService;
    
    @Autowired
    private PreguntaRepository preguntaRepository;

    @PostMapping("/evaluar")
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
        // Si no tiene score, se usa la dificultad como valor por defecto
        int valorPregunta = pregunta.getScore() > 0 ? pregunta.getScore() : pregunta.getDificultad() * 10;
        request.setValorPregunta(valorPregunta);
        
        // Obtener la postulación asociada con la pregunta
        if (pregunta.getPostulacion() == null) {
            throw new IllegalArgumentException("La pregunta no tiene una postulación asociada");
        }
        
        request.setIdPostulacion(pregunta.getPostulacion().getId());
        
        // Get puesto from convocatoria if available
        if (pregunta.getConvocatoria() != null) {
            Convocatoria convocatoria = pregunta.getConvocatoria();
            request.setPuesto(convocatoria.getPuesto());
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
    }

    @GetMapping("/mis-resultados/{postulacionId}")
    public ResponseEntity<?> verMisResultados(@PathVariable Long postulacionId) {
        // Aquí podrías implementar la lógica para obtener resultados por postulación
        return ResponseEntity.ok("Resultados para postulación: " + postulacionId);
    }

    @GetMapping("/por-entrevista/{entrevistaId}")
    public ResponseEntity<?> verResultadosPorEntrevista(@PathVariable Long entrevistaId) {
        // Aquí podrías implementar la lógica para obtener resultados por entrevista
        return ResponseEntity.ok("Resultados para entrevista: " + entrevistaId);
    }
}

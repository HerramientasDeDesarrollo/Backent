package com.example.entrevista.controller;

import com.example.entrevista.DTO.EvaluacionRequest;
import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.service.EvaluacionService;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.model.Evaluacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.repository.PostulacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private static final Logger logger = LoggerFactory.getLogger(EvaluacionController.class);

    @Autowired
    private EvaluacionService evaluacionService;
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

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
        try {
            // Obtener la postulación
            Optional<Postulacion> postulacionOpt = postulacionRepository.findById(postulacionId);
            if (!postulacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "mensaje", "No se encontró la postulación especificada"
                ));
            }
            
            Postulacion postulacion = postulacionOpt.get();
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorPostulacion(postulacionId);
            
            if (evaluaciones.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "usuarioId", postulacion.getUsuario().getId(),
                    "convocatoriaId", postulacion.getConvocatoria().getId(),
                    "tituloConvocatoria", postulacion.getConvocatoria().getTitulo(),
                    "mensaje", "No se encontraron evaluaciones para esta postulación"
                ));
            }
            
            // Calcular promedios por criterio
            double promedioClaridadEstructura = evaluaciones.stream()
                .mapToDouble(e -> e.getClaridadEstructura() != null ? e.getClaridadEstructura() : 0)
                .average().orElse(0);
                
            double promedioDominioTecnico = evaluaciones.stream()
                .mapToDouble(e -> e.getDominioTecnico() != null ? e.getDominioTecnico() : 0)
                .average().orElse(0);
                
            double promedioPertinencia = evaluaciones.stream()
                .mapToDouble(e -> e.getPertinencia() != null ? e.getPertinencia() : 0)
                .average().orElse(0);
                
            double promedioComunicacionSeguridad = evaluaciones.stream()
                .mapToDouble(e -> e.getComunicacionSeguridad() != null ? e.getComunicacionSeguridad() : 0)
                .average().orElse(0);
                
            // Calcular puntaje final (porcentaje total obtenido)
            double puntajeFinal = evaluaciones.stream()
                .mapToDouble(e -> e.getPorcentajeObtenido() != null ? e.getPorcentajeObtenido() : 0)
                .sum();
            
            // Recopilar todas las fortalezas y oportunidades de mejora
            Set<String> fortalezasSet = new HashSet<>();
            Set<String> oportunidadesSet = new HashSet<>();
            
            for (Evaluacion evaluacion : evaluaciones) {
                if (evaluacion.getEvaluacionCompleta() != null) {
                    try {
                        Map<String, Object> evaluacionObj = objectMapper.readValue(evaluacion.getEvaluacionCompleta(), Map.class);
                        
                        if (evaluacionObj.containsKey("fortalezas") && evaluacionObj.get("fortalezas") instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> fortalezas = (List<String>) evaluacionObj.get("fortalezas");
                            fortalezasSet.addAll(fortalezas);
                        }
                        
                        if (evaluacionObj.containsKey("oportunidades_mejora") && evaluacionObj.get("oportunidades_mejora") instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> oportunidades = (List<String>) evaluacionObj.get("oportunidades_mejora");
                            oportunidadesSet.addAll(oportunidades);
                        }
                    } catch (Exception e) {
                        logger.warn("Error al parsear evaluación JSON: {}", e.getMessage());
                    }
                }
            }
            
            // Obtener fecha de evaluación más reciente
            Date fechaEvaluacion = evaluaciones.stream()
                .map(Evaluacion::getFechaEvaluacion)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(new Date());
                
            Map<String, Object> resultados = new HashMap<>();
            resultados.put("success", true);
            resultados.put("usuarioId", postulacion.getUsuario().getId());
            resultados.put("convocatoriaId", postulacion.getConvocatoria().getId());
            resultados.put("tituloConvocatoria", postulacion.getConvocatoria().getTitulo());
            resultados.put("fechaEvaluacion", fechaEvaluacion);
            resultados.put("puntajeFinal", Math.round(puntajeFinal * 100) / 100.0);
            
            Map<String, Object> resumenPorCriterio = new HashMap<>();
            resumenPorCriterio.put("claridad_estructura", Math.round(promedioClaridadEstructura * 100) / 100.0);
            resumenPorCriterio.put("dominio_tecnico", Math.round(promedioDominioTecnico * 100) / 100.0);
            resumenPorCriterio.put("pertinencia", Math.round(promedioPertinencia * 100) / 100.0);
            resumenPorCriterio.put("comunicacion_seguridad", Math.round(promedioComunicacionSeguridad * 100) / 100.0);
            
            resultados.put("resumenPorCriterio", resumenPorCriterio);
            resultados.put("fortalezas", new ArrayList<>(fortalezasSet));
            resultados.put("oportunidadesMejora", new ArrayList<>(oportunidadesSet));
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            logger.error("Error al obtener resultados para postulación {}: {}", postulacionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error al procesar la solicitud: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/mis-resultados/detalle/{postulacionId}")
    public ResponseEntity<?> verMisResultadosDetalle(@PathVariable Long postulacionId) {
        try {
            // Obtener la postulación
            Optional<Postulacion> postulacionOpt = postulacionRepository.findById(postulacionId);
            if (!postulacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "mensaje", "No se encontró la postulación especificada"
                ));
            }
            
            Postulacion postulacion = postulacionOpt.get();
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorPostulacion(postulacionId);
            
            if (evaluaciones.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "usuarioId", postulacion.getUsuario().getId(),
                    "convocatoriaId", postulacion.getConvocatoria().getId(),
                    "tituloConvocatoria", postulacion.getConvocatoria().getTitulo(),
                    "mensaje", "No se encontraron evaluaciones para esta postulación",
                    "detallePreguntas", Collections.emptyList()
                ));
            }
            
            // Obtener todas las preguntas para esta postulación
            List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
            
            // Crear un mapa de pregunta -> evaluación para facilitar la búsqueda
            Map<Long, Evaluacion> evaluacionesPorPregunta = evaluaciones.stream()
                .filter(e -> e.getPregunta() != null)
                .collect(Collectors.toMap(
                    e -> e.getPregunta().getId(),
                    e -> e,
                    (existing, replacement) -> replacement // En caso de duplicados, quedarse con la última evaluación
                ));
            
            // Preparar el detalle de preguntas con sus evaluaciones
            List<Map<String, Object>> detallePreguntas = new ArrayList<>();
            
            for (Pregunta pregunta : preguntas) {
                Map<String, Object> detallePregunta = new HashMap<>();
                detallePregunta.put("numero", pregunta.getNumero());
                detallePregunta.put("tipo", pregunta.getTipoLegible());
                detallePregunta.put("pregunta", pregunta.getTextoPregunta());
                
                // Buscar la evaluación correspondiente
                Evaluacion evaluacion = evaluacionesPorPregunta.get(pregunta.getId());
                
                if (evaluacion != null) {
                    detallePregunta.put("respuestaUsuario", evaluacion.getRespuesta());
                    
                    Map<String, Object> detalleEvaluacion = new HashMap<>();
                    detalleEvaluacion.put("claridad_estructura", evaluacion.getClaridadEstructura());
                    detalleEvaluacion.put("dominio_tecnico", evaluacion.getDominioTecnico());
                    detalleEvaluacion.put("pertinencia", evaluacion.getPertinencia());
                    detalleEvaluacion.put("comunicacion_seguridad", evaluacion.getComunicacionSeguridad());
                    detalleEvaluacion.put("puntuacion_final", evaluacion.getPorcentajeObtenido());
                    
                    // Extraer fortalezas y oportunidades de mejora del JSON
                    List<String> fortalezas = Collections.emptyList();
                    List<String> oportunidadesMejora = Collections.emptyList();
                    
                    if (evaluacion.getEvaluacionCompleta() != null) {
                        try {
                            Map<String, Object> evaluacionObj = objectMapper.readValue(
                                evaluacion.getEvaluacionCompleta(), Map.class);
                            
                            if (evaluacionObj.containsKey("fortalezas") && evaluacionObj.get("fortalezas") instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> fortList = (List<String>) evaluacionObj.get("fortalezas");
                                fortalezas = fortList;
                            }
                            
                            if (evaluacionObj.containsKey("oportunidades_mejora") && 
                                evaluacionObj.get("oportunidades_mejora") instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> opList = (List<String>) evaluacionObj.get("oportunidades_mejora");
                                oportunidadesMejora = opList;
                            }
                        } catch (Exception e) {
                            logger.warn("Error al parsear evaluación JSON: {}", e.getMessage());
                        }
                    }
                    
                    detalleEvaluacion.put("fortalezas", fortalezas);
                    detalleEvaluacion.put("oportunidades_mejora", oportunidadesMejora);
                    
                    detallePregunta.put("evaluacion", detalleEvaluacion);
                } else {
                    detallePregunta.put("respuestaUsuario", "No se ha registrado respuesta");
                    detallePregunta.put("evaluacion", null);
                }
                
                detallePreguntas.add(detallePregunta);
            }
            
            // Ordenar por número de pregunta
            detallePreguntas.sort(Comparator.comparing(m -> (Integer) m.get("numero")));
            
            Map<String, Object> resultados = new HashMap<>();
            resultados.put("success", true);
            resultados.put("usuarioId", postulacion.getUsuario().getId());
            resultados.put("convocatoriaId", postulacion.getConvocatoria().getId());
            resultados.put("tituloConvocatoria", postulacion.getConvocatoria().getTitulo());
            resultados.put("detallePreguntas", detallePreguntas);
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            logger.error("Error al obtener detalles para postulación {}: {}", postulacionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Error al procesar la solicitud: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/por-entrevista/{entrevistaId}")
    public ResponseEntity<?> verResultadosPorEntrevista(@PathVariable Long entrevistaId) {
        try {
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorEntrevista(entrevistaId);
            
            if (evaluaciones.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "No se encontraron evaluaciones para este entrevistador",
                    "evaluaciones", evaluaciones
                ));
            }
            
            // Agrupar evaluaciones por postulación para presentar resultados organizados
            Map<Long, List<Evaluacion>> evaluacionesPorPostulacion = evaluaciones.stream()
                .collect(Collectors.groupingBy(e -> 
                    e.getPostulacion() != null ? e.getPostulacion().getId() : 0L));
                    
            return ResponseEntity.ok(Map.of(
                "mensaje", "Resultados encontrados",
                "totalPostulaciones", evaluacionesPorPostulacion.size(),
                "totalEvaluaciones", evaluaciones.size(),
                "resultadosPorPostulacion", evaluacionesPorPostulacion
            ));
        } catch (Exception e) {
            logger.error("Error al obtener resultados para entrevistador {}: {}", entrevistaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()));
        }
    }
}

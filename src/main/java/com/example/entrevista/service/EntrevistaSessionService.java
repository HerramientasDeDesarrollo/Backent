package com.example.entrevista.service;

import com.example.entrevista.model.*;
import com.example.entrevista.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class EntrevistaSessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EntrevistaSessionService.class);
    
    @Autowired
    private EntrevistaSessionRepository entrevistaSessionRepository;
    
    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private EvaluacionRepository evaluacionRepository;
    
    private final ObjectMapper objectMapper;
    
    public EntrevistaSessionService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    // Crear o recuperar sesión existente (idempotente)
    public EntrevistaSession crearORecuperarSesion(Long postulacionId) {
        logger.info("Creando o recuperando sesión para postulación {}", postulacionId);
        
        // Verificar si ya existe
        Optional<EntrevistaSession> sesionExistente = entrevistaSessionRepository.findByPostulacionId(postulacionId);
        if (sesionExistente.isPresent()) {
            logger.info("Sesión existente encontrada: {}", sesionExistente.get().getId());
            // Actualizar última actividad
            EntrevistaSession sesion = sesionExistente.get();
            sesion.actualizarUltimaActividad();
            return entrevistaSessionRepository.save(sesion);
        }
        
        // Crear nueva sesión
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + postulacionId));
        
        EntrevistaSession nuevaSesion = new EntrevistaSession(postulacion);
        
        // Cargar preguntas existentes si las hay
        List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
        if (!preguntas.isEmpty()) {
            try {
                String preguntasJson = objectMapper.writeValueAsString(preguntas);
                nuevaSesion.setPreguntasJson(preguntasJson);
            } catch (JsonProcessingException e) {
                logger.error("Error al serializar preguntas para postulación {}: {}", postulacionId, e.getMessage());
            }
        }
        
        EntrevistaSession sesionGuardada = entrevistaSessionRepository.save(nuevaSesion);
        logger.info("Nueva sesión creada con ID: {}", sesionGuardada.getId());
        
        return sesionGuardada;
    }
    
    // Actualizar progreso de la sesión
    public EntrevistaSession actualizarProgreso(Long sessionId, int progreso) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        sesion.setProgresoCompletado(progreso);
        sesion.actualizarUltimaActividad();
        
        if (progreso >= 100) {
            sesion.marcarComoCompletada();
        } else {
            sesion.setEstadoSesion(EstadoSesion.EN_PROGRESO);
        }
        
        return entrevistaSessionRepository.save(sesion);
    }
    
    // Agregar respuesta a la sesión
    public EntrevistaSession agregarRespuesta(Long sessionId, Long preguntaId, String respuesta) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        try {
            // Obtener respuestas existentes o crear nuevo mapa
            Map<String, Object> respuestas = new HashMap<>();
            if (sesion.getRespuestasJson() != null && !sesion.getRespuestasJson().isEmpty()) {
                respuestas = objectMapper.readValue(sesion.getRespuestasJson(), Map.class);
            }
            
            // Agregar nueva respuesta
            Map<String, Object> nuevaRespuesta = new HashMap<>();
            nuevaRespuesta.put("pregunta_id", preguntaId);
            nuevaRespuesta.put("respuesta", respuesta);
            nuevaRespuesta.put("timestamp", LocalDateTime.now().toString());
            
            respuestas.put("pregunta_" + preguntaId, nuevaRespuesta);
            
            // Actualizar JSON
            String respuestasJson = objectMapper.writeValueAsString(respuestas);
            sesion.setRespuestasJson(respuestasJson);
            sesion.actualizarUltimaActividad();
            
            return entrevistaSessionRepository.save(sesion);
            
        } catch (Exception e) {
            logger.error("Error al agregar respuesta a sesión {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Error al procesar respuesta");
        }
    }
    
    // Agregar evaluación a la sesión
    public EntrevistaSession agregarEvaluacion(Long sessionId, Evaluacion evaluacion) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        try {
            // Obtener evaluaciones existentes o crear nuevo mapa
            Map<String, Object> evaluaciones = new HashMap<>();
            if (sesion.getEvaluacionesJson() != null && !sesion.getEvaluacionesJson().isEmpty()) {
                evaluaciones = objectMapper.readValue(sesion.getEvaluacionesJson(), Map.class);
            }
            
            // Crear objeto evaluación simplificado
            Map<String, Object> evaluacionData = new HashMap<>();
            evaluacionData.put("pregunta_id", evaluacion.getPregunta().getId());
            evaluacionData.put("claridad_estructura", evaluacion.getClaridadEstructura());
            evaluacionData.put("dominio_tecnico", evaluacion.getDominioTecnico());
            evaluacionData.put("pertinencia", evaluacion.getPertinencia());
            evaluacionData.put("comunicacion_seguridad", evaluacion.getComunicacionSeguridad());
            evaluacionData.put("porcentaje_obtenido", evaluacion.getPorcentajeObtenido());
            evaluacionData.put("evaluacion_completa", evaluacion.getEvaluacionCompleta());
            evaluacionData.put("timestamp", LocalDateTime.now().toString());
            
            evaluaciones.put("pregunta_" + evaluacion.getPregunta().getId(), evaluacionData);
            
            // Actualizar JSON
            String evaluacionesJson = objectMapper.writeValueAsString(evaluaciones);
            sesion.setEvaluacionesJson(evaluacionesJson);
            sesion.actualizarUltimaActividad();
            
            // Calcular progreso basado en número de evaluaciones
            int totalEvaluaciones = evaluaciones.size();
            int progreso = Math.min(100, (totalEvaluaciones * 10)); // Asumiendo 10 preguntas máximo
            sesion.setProgresoCompletado(progreso);
            
            if (progreso >= 100) {
                sesion.marcarComoCompletada();
                calcularPuntuacionTotal(sesion, evaluaciones);
            }
            
            return entrevistaSessionRepository.save(sesion);
            
        } catch (Exception e) {
            logger.error("Error al agregar evaluación a sesión {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Error al procesar evaluación");
        }
    }
    
    // Finalizar sesión completa
    public EntrevistaSession finalizarSesion(Long sessionId) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        sesion.marcarComoCompletada();
        
        // Calcular puntuación total si tiene evaluaciones
        if (sesion.getEvaluacionesJson() != null && !sesion.getEvaluacionesJson().isEmpty()) {
            try {
                Map<String, Object> evaluaciones = objectMapper.readValue(sesion.getEvaluacionesJson(), Map.class);
                calcularPuntuacionTotal(sesion, evaluaciones);
            } catch (Exception e) {
                logger.error("Error al calcular puntuación total para sesión {}: {}", sessionId, e.getMessage());
            }
        }
        
        return entrevistaSessionRepository.save(sesion);
    }
    
    // Obtener resultados completos de la sesión
    public Map<String, Object> obtenerResultadosCompletos(Long sessionId) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        Map<String, Object> resultados = new HashMap<>();
        
        try {
            // Información básica de la sesión
            resultados.put("session_id", sesion.getId());
            resultados.put("postulacion_id", sesion.getPostulacion().getId());
            resultados.put("estado", sesion.getEstadoSesion().name());
            resultados.put("progreso", sesion.getProgresoCompletado());
            resultados.put("completada", sesion.getEsCompletada());
            resultados.put("puntuacion_total", sesion.getPuntuacionTotal());
            resultados.put("fecha_inicio", sesion.getFechaInicio());
            resultados.put("fecha_finalizacion", sesion.getFechaFinalizacion());
            
            // Deserializar JSONs
            if (sesion.getPreguntasJson() != null) {
                resultados.put("preguntas", objectMapper.readValue(sesion.getPreguntasJson(), Object.class));
            }
            
            if (sesion.getRespuestasJson() != null) {
                resultados.put("respuestas", objectMapper.readValue(sesion.getRespuestasJson(), Object.class));
            }
            
            if (sesion.getEvaluacionesJson() != null) {
                resultados.put("evaluaciones", objectMapper.readValue(sesion.getEvaluacionesJson(), Object.class));
            }
            
            if (sesion.getMetadatosJson() != null) {
                resultados.put("metadatos", objectMapper.readValue(sesion.getMetadatosJson(), Object.class));
            }
            
            resultados.put("success", true);
            
        } catch (Exception e) {
            logger.error("Error al obtener resultados para sesión {}: {}", sessionId, e.getMessage());
            resultados.put("success", false);
            resultados.put("error", "Error al procesar resultados: " + e.getMessage());
        }
        
        return resultados;
    }
    
    // Obtener resumen de resultados
    public Map<String, Object> obtenerResumenResultados(Long sessionId) {
        EntrevistaSession sesion = entrevistaSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sesión no encontrada con ID: " + sessionId));
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("session_id", sesion.getId());
        resumen.put("postulacion_id", sesion.getPostulacion().getId());
        resumen.put("estado", sesion.getEstadoSesion().getDescripcion());
        resumen.put("progreso", sesion.getProgresoCompletado());
        resumen.put("completada", sesion.getEsCompletada());
        resumen.put("puntuacion_total", sesion.getPuntuacionTotal());
        
        // Estadísticas rápidas si está completada
        if (sesion.getEsCompletada() && sesion.getEvaluacionesJson() != null) {
            try {
                Map<String, Object> evaluaciones = objectMapper.readValue(sesion.getEvaluacionesJson(), Map.class);
                
                int totalPreguntas = evaluaciones.size();
                double promedioGeneral = 0.0;
                
                for (Object eval : evaluaciones.values()) {
                    if (eval instanceof Map) {
                        Map<String, Object> evalMap = (Map<String, Object>) eval;
                        Object porcentaje = evalMap.get("porcentaje_obtenido");
                        if (porcentaje instanceof Number) {
                            promedioGeneral += ((Number) porcentaje).doubleValue();
                        }
                    }
                }
                
                if (totalPreguntas > 0) {
                    promedioGeneral = promedioGeneral / totalPreguntas;
                }
                
                resumen.put("total_preguntas", totalPreguntas);
                resumen.put("promedio_general", Math.round(promedioGeneral * 100.0) / 100.0);
                
            } catch (Exception e) {
                logger.error("Error al calcular estadísticas para sesión {}: {}", sessionId, e.getMessage());
            }
        }
        
        resumen.put("success", true);
        return resumen;
    }
    
    // Buscar sesión por postulación
    public Optional<EntrevistaSession> buscarPorPostulacion(Long postulacionId) {
        return entrevistaSessionRepository.findByPostulacionId(postulacionId);
    }
    
    // Verificar si existe sesión para postulación
    public boolean existeSesionParaPostulacion(Long postulacionId) {
        return entrevistaSessionRepository.existsByPostulacionId(postulacionId);
    }
    
    // Método privado para calcular puntuación total
    private void calcularPuntuacionTotal(EntrevistaSession sesion, Map<String, Object> evaluaciones) {
        try {
            double total = 0.0;
            int contador = 0;
            
            for (Object eval : evaluaciones.values()) {
                if (eval instanceof Map) {
                    Map<String, Object> evalMap = (Map<String, Object>) eval;
                    Object porcentaje = evalMap.get("porcentaje_obtenido");
                    if (porcentaje instanceof Number) {
                        total += ((Number) porcentaje).doubleValue();
                        contador++;
                    }
                }
            }
            
            if (contador > 0) {
                sesion.setPuntuacionTotal(total / contador);
            }
            
        } catch (Exception e) {
            logger.error("Error al calcular puntuación total: {}", e.getMessage());
        }
    }
}

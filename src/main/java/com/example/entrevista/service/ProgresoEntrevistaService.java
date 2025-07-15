package com.example.entrevista.service;

import com.example.entrevista.model.*;
import com.example.entrevista.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgresoEntrevistaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProgresoEntrevistaService.class);
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private EvaluacionRepository evaluacionRepository;
    
    /**
     * Obtiene el progreso detallado de una entrevista (postulación)
     * @param postulacionId ID de la postulación
     * @return Mapa con información detallada del progreso
     */
    public Map<String, Object> obtenerProgresoDetallado(Long postulacionId) {
        logger.info("Obteniendo progreso detallado para postulación: {}", postulacionId);
        
        // Obtener todas las preguntas de la postulación
        List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
        
        // Obtener todas las evaluaciones (respuestas) de la postulación
        List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(postulacionId);
        
        // Crear un mapa de pregunta_id -> evaluación para búsqueda rápida
        Map<Long, Evaluacion> evaluacionesPorPregunta = evaluaciones.stream()
            .collect(Collectors.toMap(
                eval -> eval.getPregunta().getId(),
                eval -> eval,
                (existing, replacement) -> existing // Si hay duplicados, mantener el primero
            ));
        
        // Construir información detallada de cada pregunta
        List<Map<String, Object>> preguntasDetalle = new ArrayList<>();
        int preguntasRespondidas = 0;
        
        for (Pregunta pregunta : preguntas) {
            Map<String, Object> preguntaInfo = new HashMap<>();
            preguntaInfo.put("id", pregunta.getId());
            preguntaInfo.put("numero", pregunta.getNumero());
            preguntaInfo.put("texto", pregunta.getTextoPregunta());
            preguntaInfo.put("tipo", pregunta.getTipoLegible());
            preguntaInfo.put("score", pregunta.getScore());
            
            // Verificar si la pregunta ha sido respondida
            Evaluacion evaluacion = evaluacionesPorPregunta.get(pregunta.getId());
            if (evaluacion != null) {
                preguntaInfo.put("respondida", true);
                preguntaInfo.put("respuesta", evaluacion.getRespuesta());
                preguntaInfo.put("porcentaje_obtenido", evaluacion.getPorcentajeObtenido());
                preguntaInfo.put("fecha_respuesta", evaluacion.getFechaEvaluacion());
                preguntasRespondidas++;
            } else {
                preguntaInfo.put("respondida", false);
                preguntaInfo.put("respuesta", null);
                preguntaInfo.put("porcentaje_obtenido", null);
                preguntaInfo.put("fecha_respuesta", null);
            }
            
            preguntasDetalle.add(preguntaInfo);
        }
        
        // Calcular estadísticas generales
        int totalPreguntas = preguntas.size();
        double porcentajeProgreso = totalPreguntas > 0 ? (double) preguntasRespondidas / totalPreguntas * 100 : 0;
        
        // Calcular puntuación promedio si hay respuestas
        double puntuacionPromedio = 0.0;
        if (preguntasRespondidas > 0) {
            double sumaPorcentajes = evaluaciones.stream()
                .filter(eval -> eval.getPorcentajeObtenido() != null)
                .mapToDouble(Evaluacion::getPorcentajeObtenido)
                .sum();
            puntuacionPromedio = sumaPorcentajes / preguntasRespondidas;
        }
        
        // Construir respuesta
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("postulacion_id", postulacionId);
        progreso.put("total_preguntas", totalPreguntas);
        progreso.put("preguntas_respondidas", preguntasRespondidas);
        progreso.put("preguntas_pendientes", totalPreguntas - preguntasRespondidas);
        progreso.put("porcentaje_progreso", Math.round(porcentajeProgreso * 100.0) / 100.0);
        progreso.put("puntuacion_promedio", Math.round(puntuacionPromedio * 100.0) / 100.0);
        progreso.put("entrevista_completa", preguntasRespondidas == totalPreguntas && totalPreguntas > 0);
        progreso.put("preguntas_detalle", preguntasDetalle);
        
        // Identificar próxima pregunta a responder
        Optional<Pregunta> proximaPregunta = preguntas.stream()
            .filter(p -> !evaluacionesPorPregunta.containsKey(p.getId()))
            .min(Comparator.comparingInt(Pregunta::getNumero));
        
        if (proximaPregunta.isPresent()) {
            Map<String, Object> proxima = new HashMap<>();
            proxima.put("id", proximaPregunta.get().getId());
            proxima.put("numero", proximaPregunta.get().getNumero());
            proxima.put("texto", proximaPregunta.get().getTextoPregunta());
            progreso.put("proxima_pregunta", proxima);
        } else {
            progreso.put("proxima_pregunta", null);
        }
        
        return progreso;
    }
    
    /**
     * Obtiene un resumen simple del progreso
     * @param postulacionId ID de la postulación
     * @return Resumen simple con números principales
     */
    public Map<String, Object> obtenerResumenProgreso(Long postulacionId) {
        logger.debug("Obteniendo resumen de progreso para postulación: {}", postulacionId);
        
        long totalPreguntas = preguntaRepository.countByPostulacionId(postulacionId);
        long preguntasRespondidas = evaluacionRepository.countByPostulacionId(postulacionId);
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("postulacion_id", postulacionId);
        resumen.put("total_preguntas", totalPreguntas);
        resumen.put("preguntas_respondidas", preguntasRespondidas);
        resumen.put("preguntas_pendientes", totalPreguntas - preguntasRespondidas);
        
        if (totalPreguntas > 0) {
            double porcentaje = (double) preguntasRespondidas / totalPreguntas * 100;
            resumen.put("porcentaje_progreso", Math.round(porcentaje * 100.0) / 100.0);
            resumen.put("entrevista_completa", preguntasRespondidas == totalPreguntas);
        } else {
            resumen.put("porcentaje_progreso", 0.0);
            resumen.put("entrevista_completa", false);
        }
        
        return resumen;
    }
    
    /**
     * Verifica si una pregunta específica ha sido respondida
     * @param preguntaId ID de la pregunta
     * @return true si la pregunta ha sido respondida, false en caso contrario
     */
    public boolean preguntaRespondida(Long preguntaId) {
        return evaluacionRepository.countByPreguntaId(preguntaId) > 0;
    }
    
    /**
     * Obtiene la lista de preguntas que faltan por responder
     * @param postulacionId ID de la postulación
     * @return Lista de preguntas sin responder
     */
    public List<Pregunta> obtenerPreguntasPendientes(Long postulacionId) {
        List<Pregunta> todasLasPreguntas = preguntaRepository.findByPostulacionId(postulacionId);
        List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(postulacionId);
        
        Set<Long> preguntasRespondidas = evaluaciones.stream()
            .map(eval -> eval.getPregunta().getId())
            .collect(Collectors.toSet());
        
        return todasLasPreguntas.stream()
            .filter(pregunta -> !preguntasRespondidas.contains(pregunta.getId()))
            .sorted(Comparator.comparingInt(Pregunta::getNumero))
            .collect(Collectors.toList());
    }
}

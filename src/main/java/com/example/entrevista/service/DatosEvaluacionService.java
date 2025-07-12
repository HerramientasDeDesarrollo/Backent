package com.example.entrevista.service;

import com.example.entrevista.model.Evaluacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.EvaluacionRepository;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.PreguntaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio optimizado para obtener datos de evaluaciones de manera eficiente
 * Mejora la lógica de consultas y reduce el número de llamadas a la base de datos
 */
@Service
public class DatosEvaluacionService {

    private static final Logger logger = LoggerFactory.getLogger(DatosEvaluacionService.class);

    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private EvaluacionRepository evaluacionRepository;

    /**
     * DTO para encapsular todos los datos relacionados con una postulación
     */
    public static class DatosPostulacion {
        private Postulacion postulacion;
        private List<Pregunta> preguntas;
        private List<Evaluacion> evaluaciones;
        private Map<Long, Evaluacion> evaluacionesPorPregunta;
        private boolean datosCompletos;
        private List<String> problemasDetectados;

        // Constructores
        public DatosPostulacion() {
            this.problemasDetectados = new ArrayList<>();
        }

        // Getters y Setters
        public Postulacion getPostulacion() { return postulacion; }
        public void setPostulacion(Postulacion postulacion) { this.postulacion = postulacion; }
        
        public List<Pregunta> getPreguntas() { return preguntas; }
        public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
        
        public List<Evaluacion> getEvaluaciones() { return evaluaciones; }
        public void setEvaluaciones(List<Evaluacion> evaluaciones) { this.evaluaciones = evaluaciones; }
        
        public Map<Long, Evaluacion> getEvaluacionesPorPregunta() { return evaluacionesPorPregunta; }
        public void setEvaluacionesPorPregunta(Map<Long, Evaluacion> evaluacionesPorPregunta) { 
            this.evaluacionesPorPregunta = evaluacionesPorPregunta; 
        }
        
        public boolean isDatosCompletos() { return datosCompletos; }
        public void setDatosCompletos(boolean datosCompletos) { this.datosCompletos = datosCompletos; }
        
        public List<String> getProblemasDetectados() { return problemasDetectados; }
        public void setProblemasDetectados(List<String> problemasDetectados) { 
            this.problemasDetectados = problemasDetectados; 
        }

        // Métodos de utilidad
        public boolean tienePreguntas() {
            return preguntas != null && !preguntas.isEmpty();
        }

        public boolean tieneEvaluaciones() {
            return evaluaciones != null && !evaluaciones.isEmpty();
        }

        public int getTotalPreguntas() {
            return preguntas != null ? preguntas.size() : 0;
        }

        public int getTotalEvaluaciones() {
            return evaluaciones != null ? evaluaciones.size() : 0;
        }

        public boolean numerosCoinciden() {
            return getTotalPreguntas() == getTotalEvaluaciones();
        }

        public Evaluacion getEvaluacionParaPregunta(Long preguntaId) {
            return evaluacionesPorPregunta != null ? evaluacionesPorPregunta.get(preguntaId) : null;
        }
    }

    /**
     * Método principal optimizado que obtiene todos los datos de una postulación
     * en una sola operación, minimizando las consultas a la base de datos
     */
    public DatosPostulacion obtenerDatosCompletos(Long postulacionId) {
        logger.info("Obteniendo datos completos para postulación {}", postulacionId);
        
        DatosPostulacion datos = new DatosPostulacion();
        
        try {
            // 1. Obtener postulación (1 consulta)
            Optional<Postulacion> postulacionOpt = postulacionRepository.findById(postulacionId);
            if (!postulacionOpt.isPresent()) {
                datos.getProblemasDetectados().add("Postulación no encontrada");
                logger.warn("Postulación {} no encontrada", postulacionId);
                return datos;
            }
            datos.setPostulacion(postulacionOpt.get());

            // 2. Obtener preguntas y evaluaciones en paralelo (2 consultas)
            List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
            List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(postulacionId);
            
            datos.setPreguntas(preguntas);
            datos.setEvaluaciones(evaluaciones);

            logger.debug("Postulación {}: {} preguntas, {} evaluaciones obtenidas", 
                        postulacionId, preguntas.size(), evaluaciones.size());

            // 3. Crear mapa optimizado para búsquedas rápidas
            Map<Long, Evaluacion> evaluacionesPorPregunta = construirMapaEvaluaciones(evaluaciones);
            datos.setEvaluacionesPorPregunta(evaluacionesPorPregunta);

            // 4. Validar integridad de datos
            validarIntegridad(datos);

            logger.info("Datos completos obtenidos para postulación {}. Válidos: {}", 
                       postulacionId, datos.isDatosCompletos());

        } catch (Exception e) {
            logger.error("Error obteniendo datos para postulación {}: {}", postulacionId, e.getMessage(), e);
            datos.getProblemasDetectados().add("Error interno al obtener datos: " + e.getMessage());
        }

        return datos;
    }

    /**
     * Versión optimizada específica para obtener solo evaluaciones con validación
     */
    public List<Evaluacion> obtenerEvaluacionesValidadas(Long postulacionId) {
        logger.debug("Obteniendo evaluaciones validadas para postulación {}", postulacionId);
        
        DatosPostulacion datos = obtenerDatosCompletos(postulacionId);
        
        if (!datos.isDatosCompletos()) {
            logger.warn("Datos incompletos para postulación {}: {}", 
                       postulacionId, datos.getProblemasDetectados());
            return Collections.emptyList();
        }
        
        return datos.getEvaluaciones();
    }

    /**
     * Obtiene solo las preguntas con verificación de existencia
     */
    public List<Pregunta> obtenerPreguntasValidadas(Long postulacionId) {
        logger.debug("Obteniendo preguntas validadas para postulación {}", postulacionId);
        
        // Verificación rápida de existencia de postulación
        if (!postulacionRepository.existsById(postulacionId)) {
            logger.warn("Postulación {} no existe", postulacionId);
            return Collections.emptyList();
        }
        
        List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
        logger.debug("Encontradas {} preguntas para postulación {}", preguntas.size(), postulacionId);
        
        return preguntas;
    }

    /**
     * Verifica si una postulación tiene datos suficientes para generar resultados
     */
    public boolean puedeGenerarResultados(Long postulacionId) {
        logger.debug("Verificando si postulación {} puede generar resultados", postulacionId);
        
        // Optimización: verificar conteos antes de cargar todos los datos
        if (!postulacionRepository.existsById(postulacionId)) {
            logger.debug("Postulación {} no existe", postulacionId);
            return false;
        }

        long totalPreguntas = preguntaRepository.countByPostulacionId(postulacionId);
        long totalEvaluaciones = evaluacionRepository.countByPostulacionId(postulacionId);

        if (totalPreguntas == 0 || totalEvaluaciones == 0) {
            logger.debug("Postulación {}: {} preguntas, {} evaluaciones - insuficientes", 
                        postulacionId, totalPreguntas, totalEvaluaciones);
            return false;
        }

        if (totalPreguntas != totalEvaluaciones) {
            logger.debug("Postulación {}: números no coinciden - {} preguntas vs {} evaluaciones", 
                        postulacionId, totalPreguntas, totalEvaluaciones);
            return false;
        }

        // Si los conteos son correctos, verificar calidad de datos
        DatosPostulacion datos = obtenerDatosCompletos(postulacionId);
        return datos.isDatosCompletos();
    }

    /**
     * Obtiene estadísticas rápidas sin cargar todos los datos
     */
    public Map<String, Object> obtenerEstadisticasRapidas(Long postulacionId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("postulacion_existe", postulacionRepository.existsById(postulacionId));
            stats.put("total_preguntas", preguntaRepository.countByPostulacionId(postulacionId));
            stats.put("total_evaluaciones", evaluacionRepository.countByPostulacionId(postulacionId));
            
            // Calcular evaluaciones completas sin cargar toda la data
            long evaluacionesCompletas = evaluacionRepository.countEvaluacionesCompletas(postulacionId);
            stats.put("evaluaciones_completas", evaluacionesCompletas);
            
            stats.put("puede_generar_resultados", puedeGenerarResultados(postulacionId));
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas para postulación {}: {}", postulacionId, e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    // Métodos privados de utilidad

    private Map<Long, Evaluacion> construirMapaEvaluaciones(List<Evaluacion> evaluaciones) {
        return evaluaciones.stream()
            .filter(e -> e.getPregunta() != null)
            .collect(Collectors.toMap(
                e -> e.getPregunta().getId(),
                e -> e,
                (existing, replacement) -> {
                    logger.warn("Duplicado encontrado para pregunta ID: {}. Usando evaluación más reciente.", 
                               existing.getPregunta().getId());
                    // Mantener la evaluación más reciente
                    return replacement.getFechaEvaluacion().after(existing.getFechaEvaluacion()) ? 
                           replacement : existing;
                }
            ));
    }

    private void validarIntegridad(DatosPostulacion datos) {
        List<String> problemas = datos.getProblemasDetectados();
        
        // Validación 1: Preguntas
        if (!datos.tienePreguntas()) {
            problemas.add("No hay preguntas asociadas a la postulación");
        }

        // Validación 2: Evaluaciones
        if (!datos.tieneEvaluaciones()) {
            problemas.add("No hay evaluaciones registradas");
        }

        // Validación 3: Números coinciden
        if (datos.tienePreguntas() && datos.tieneEvaluaciones() && !datos.numerosCoinciden()) {
            problemas.add("Número de preguntas (" + datos.getTotalPreguntas() + 
                         ") no coincide con evaluaciones (" + datos.getTotalEvaluaciones() + ")");
        }

        // Validación 4: Calidad de evaluaciones
        if (datos.tieneEvaluaciones()) {
            long evaluacionesIncompletas = datos.getEvaluaciones().stream()
                .filter(e -> tieneEvaluacionIncompleta(e))
                .count();
            
            if (evaluacionesIncompletas > 0) {
                problemas.add("Hay " + evaluacionesIncompletas + " evaluaciones con datos incompletos");
            }
        }

        // Validación 5: Correspondencia pregunta-evaluación
        if (datos.tienePreguntas() && datos.tieneEvaluaciones()) {
            Set<Long> preguntaIds = datos.getPreguntas().stream()
                .map(Pregunta::getId)
                .collect(Collectors.toSet());
            
            Set<Long> evaluacionPreguntaIds = datos.getEvaluacionesPorPregunta().keySet();
            
            if (!preguntaIds.equals(evaluacionPreguntaIds)) {
                problemas.add("Hay preguntas sin evaluación o evaluaciones sin pregunta correspondiente");
            }
        }

        // Determinar si los datos están completos
        datos.setDatosCompletos(problemas.isEmpty());
    }

    private boolean tieneEvaluacionIncompleta(Evaluacion evaluacion) {
        return evaluacion.getClaridadEstructura() == null ||
               evaluacion.getDominioTecnico() == null ||
               evaluacion.getPertinencia() == null ||
               evaluacion.getComunicacionSeguridad() == null ||
               evaluacion.getPorcentajeObtenido() == null ||
               evaluacion.getEvaluacionCompleta() == null ||
               evaluacion.getEvaluacionCompleta().trim().isEmpty();
    }
}

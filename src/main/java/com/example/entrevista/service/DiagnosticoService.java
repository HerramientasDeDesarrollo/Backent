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

@Service
public class DiagnosticoService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoService.class);

    @Autowired
    private EvaluacionRepository evaluacionRepository;
    
    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;

    /**
     * Realiza un diagnóstico completo de una postulación
     * para verificar el estado de las evaluaciones
     */
    public Map<String, Object> diagnosticarPostulacion(Long postulacionId) {
        Map<String, Object> diagnostico = new HashMap<>();
        
        try {
            // 1. Verificar que la postulación existe
            Optional<Postulacion> postulacionOpt = postulacionRepository.findById(postulacionId);
            if (!postulacionOpt.isPresent()) {
                return crearDiagnosticoError("Postulación no encontrada con ID: " + postulacionId);
            }
            
            Postulacion postulacion = postulacionOpt.get();
            diagnostico.put("postulacion_encontrada", true);
            diagnostico.put("postulacion_id", postulacionId);
            diagnostico.put("usuario_id", postulacion.getUsuario().getId());
            diagnostico.put("convocatoria_id", postulacion.getConvocatoria().getId());
            
            // 2. Verificar preguntas asociadas
            List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);
            diagnostico.put("total_preguntas", preguntas.size());
            
            if (preguntas.isEmpty()) {
                diagnostico.put("problema_detectado", "No hay preguntas asociadas a esta postulación");
                return diagnostico;
            }
            
            // 3. Verificar evaluaciones
            List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(postulacionId);
            diagnostico.put("total_evaluaciones", evaluaciones.size());
            
            // 4. Análisis detallado de preguntas vs evaluaciones
            Map<String, Object> analisisPreguntas = analizarPreguntasVsEvaluaciones(preguntas, evaluaciones);
            diagnostico.put("analisis_preguntas", analisisPreguntas);
            
            // 5. Verificar integridad de datos de evaluaciones
            Map<String, Object> integridad = verificarIntegridadEvaluaciones(evaluaciones);
            diagnostico.put("integridad_evaluaciones", integridad);
            
            // 6. Verificar posibles problemas
            List<String> problemasDetectados = detectarProblemas(preguntas, evaluaciones);
            diagnostico.put("problemas_detectados", problemasDetectados);
            
            // 7. Sugerencias de reparación
            List<String> sugerencias = generarSugerenciasReparacion(preguntas, evaluaciones);
            diagnostico.put("sugerencias_reparacion", sugerencias);
            
            diagnostico.put("diagnostico_exitoso", true);
            diagnostico.put("timestamp", new Date());
            
        } catch (Exception e) {
            logger.error("Error durante diagnóstico de postulación {}: {}", postulacionId, e.getMessage(), e);
            diagnostico.put("error", "Error durante el diagnóstico: " + e.getMessage());
            diagnostico.put("diagnostico_exitoso", false);
        }
        
        return diagnostico;
    }

    /**
     * Obtiene un reporte de salud general del sistema de evaluaciones
     */
    public Map<String, Object> reporteSaludSistema() {
        Map<String, Object> reporte = new HashMap<>();
        
        try {
            // Estadísticas generales
            long totalPostulaciones = postulacionRepository.count();
            long totalPreguntas = preguntaRepository.count();
            long totalEvaluaciones = evaluacionRepository.count();
            
            reporte.put("total_postulaciones", totalPostulaciones);
            reporte.put("total_preguntas", totalPreguntas);
            reporte.put("total_evaluaciones", totalEvaluaciones);
            
            // Verificar consistencia general
            List<Map<String, Object>> postulacionesProblematicas = new ArrayList<>();
            
            // Buscar postulaciones con preguntas pero sin evaluaciones
            List<Postulacion> todasPostulaciones = postulacionRepository.findAll();
            for (Postulacion postulacion : todasPostulaciones) {
                List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacion.getId());
                List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(postulacion.getId());
                
                if (!preguntas.isEmpty() && evaluaciones.isEmpty()) {
                    Map<String, Object> problema = new HashMap<>();
                    problema.put("postulacion_id", postulacion.getId());
                    problema.put("usuario_id", postulacion.getUsuario().getId());
                    problema.put("total_preguntas", preguntas.size());
                    problema.put("total_evaluaciones", evaluaciones.size());
                    problema.put("problema", "Tiene preguntas pero ninguna evaluación");
                    postulacionesProblematicas.add(problema);
                } else if (preguntas.size() != evaluaciones.size() && !evaluaciones.isEmpty()) {
                    Map<String, Object> problema = new HashMap<>();
                    problema.put("postulacion_id", postulacion.getId());
                    problema.put("usuario_id", postulacion.getUsuario().getId());
                    problema.put("total_preguntas", preguntas.size());
                    problema.put("total_evaluaciones", evaluaciones.size());
                    problema.put("problema", "Número de preguntas y evaluaciones no coincide");
                    postulacionesProblematicas.add(problema);
                }
            }
            
            reporte.put("postulaciones_problematicas", postulacionesProblematicas);
            reporte.put("total_problemas_detectados", postulacionesProblematicas.size());
            reporte.put("porcentaje_salud", calcularPorcentajeSalud(totalPostulaciones, postulacionesProblematicas.size()));
            reporte.put("timestamp", new Date());
            
        } catch (Exception e) {
            logger.error("Error generando reporte de salud: {}", e.getMessage(), e);
            reporte.put("error", "Error generando reporte: " + e.getMessage());
        }
        
        return reporte;
    }

    /**
     * Intenta reparar automáticamente problemas detectados
     */
    public Map<String, Object> intentarReparacionAutomatica(Long postulacionId) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> accionesTomadas = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        
        try {
            // Realizar diagnóstico primero
            Map<String, Object> diagnostico = diagnosticarPostulacion(postulacionId);
            
            if (!(Boolean) diagnostico.getOrDefault("diagnostico_exitoso", false)) {
                resultado.put("error", "No se pudo realizar el diagnóstico");
                return resultado;
            }
            
            @SuppressWarnings("unchecked")
            List<String> problemas = (List<String>) diagnostico.get("problemas_detectados");
            
            if (problemas == null || problemas.isEmpty()) {
                resultado.put("mensaje", "No se detectaron problemas que requieran reparación");
                resultado.put("reparacion_necesaria", false);
                return resultado;
            }
            
            // Intentar reparaciones específicas
            for (String problema : problemas) {
                if (problema.contains("evaluaciones con datos nulos")) {
                    int reparadas = repararEvaluacionesConDatosNulos(postulacionId);
                    if (reparadas > 0) {
                        accionesTomadas.add("Reparadas " + reparadas + " evaluaciones con datos nulos");
                    }
                }
                
                if (problema.contains("evaluaciones sin JSON válido")) {
                    int reparadas = repararEvaluacionesSinJSON(postulacionId);
                    if (reparadas > 0) {
                        accionesTomadas.add("Reparadas " + reparadas + " evaluaciones sin JSON válido");
                    }
                }
            }
            
            resultado.put("acciones_tomadas", accionesTomadas);
            resultado.put("errores", errores);
            resultado.put("reparacion_exitosa", errores.isEmpty());
            resultado.put("timestamp", new Date());
            
        } catch (Exception e) {
            logger.error("Error durante reparación automática: {}", e.getMessage(), e);
            errores.add("Error durante reparación: " + e.getMessage());
            resultado.put("errores", errores);
            resultado.put("reparacion_exitosa", false);
        }
        
        return resultado;
    }

    // Métodos auxiliares privados

    private Map<String, Object> crearDiagnosticoError(String mensaje) {
        return Map.of(
            "diagnostico_exitoso", false,
            "error", mensaje,
            "timestamp", new Date()
        );
    }

    private Map<String, Object> analizarPreguntasVsEvaluaciones(List<Pregunta> preguntas, List<Evaluacion> evaluaciones) {
        Map<String, Object> analisis = new HashMap<>();
        
        // Mapear evaluaciones por pregunta ID
        Map<Long, Evaluacion> evaluacionesPorPregunta = new HashMap<>();
        for (Evaluacion eval : evaluaciones) {
            if (eval.getPregunta() != null) {
                evaluacionesPorPregunta.put(eval.getPregunta().getId(), eval);
            }
        }
        
        List<Map<String, Object>> detallePreguntas = new ArrayList<>();
        int preguntasSinEvaluacion = 0;
        
        for (Pregunta pregunta : preguntas) {
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("pregunta_id", pregunta.getId());
            detalle.put("numero", pregunta.getNumero());
            detalle.put("tipo", pregunta.getTipoLegible());
            
            Evaluacion evaluacion = evaluacionesPorPregunta.get(pregunta.getId());
            if (evaluacion != null) {
                detalle.put("tiene_evaluacion", true);
                detalle.put("evaluacion_id", evaluacion.getId());
                detalle.put("fecha_evaluacion", evaluacion.getFechaEvaluacion());
            } else {
                detalle.put("tiene_evaluacion", false);
                preguntasSinEvaluacion++;
            }
            
            detallePreguntas.add(detalle);
        }
        
        analisis.put("detalle_preguntas", detallePreguntas);
        analisis.put("preguntas_sin_evaluacion", preguntasSinEvaluacion);
        analisis.put("porcentaje_completado", preguntas.size() > 0 ? 
            ((double)(preguntas.size() - preguntasSinEvaluacion) / preguntas.size()) * 100 : 0);
        
        return analisis;
    }

    private Map<String, Object> verificarIntegridadEvaluaciones(List<Evaluacion> evaluaciones) {
        Map<String, Object> integridad = new HashMap<>();
        
        int evaluacionesConDatosNulos = 0;
        int evaluacionesSinJSON = 0;
        int evaluacionesCompletas = 0;
        
        for (Evaluacion eval : evaluaciones) {
            boolean tieneProblemas = false;
            
            // Verificar campos nulos
            if (eval.getClaridadEstructura() == null || eval.getDominioTecnico() == null ||
                eval.getPertinencia() == null || eval.getComunicacionSeguridad() == null ||
                eval.getPorcentajeObtenido() == null) {
                evaluacionesConDatosNulos++;
                tieneProblemas = true;
            }
            
            // Verificar JSON
            if (eval.getEvaluacionCompleta() == null || eval.getEvaluacionCompleta().trim().isEmpty()) {
                evaluacionesSinJSON++;
                tieneProblemas = true;
            }
            
            if (!tieneProblemas) {
                evaluacionesCompletas++;
            }
        }
        
        integridad.put("total_evaluaciones", evaluaciones.size());
        integridad.put("evaluaciones_completas", evaluacionesCompletas);
        integridad.put("evaluaciones_con_datos_nulos", evaluacionesConDatosNulos);
        integridad.put("evaluaciones_sin_json", evaluacionesSinJSON);
        integridad.put("porcentaje_integridad", evaluaciones.size() > 0 ? 
            ((double)evaluacionesCompletas / evaluaciones.size()) * 100 : 0);
        
        return integridad;
    }

    private List<String> detectarProblemas(List<Pregunta> preguntas, List<Evaluacion> evaluaciones) {
        List<String> problemas = new ArrayList<>();
        
        if (preguntas.isEmpty()) {
            problemas.add("No hay preguntas asociadas a la postulación");
        }
        
        if (evaluaciones.isEmpty()) {
            problemas.add("No hay evaluaciones registradas");
        } else {
            // Verificar integridad
            long evaluacionesIncompletas = evaluaciones.stream()
                .filter(e -> e.getClaridadEstructura() == null || e.getDominioTecnico() == null ||
                           e.getPertinencia() == null || e.getComunicacionSeguridad() == null)
                .count();
            
            if (evaluacionesIncompletas > 0) {
                problemas.add("Hay " + evaluacionesIncompletas + " evaluaciones con datos nulos");
            }
            
            long evaluacionesSinJSON = evaluaciones.stream()
                .filter(e -> e.getEvaluacionCompleta() == null || e.getEvaluacionCompleta().trim().isEmpty())
                .count();
            
            if (evaluacionesSinJSON > 0) {
                problemas.add("Hay " + evaluacionesSinJSON + " evaluaciones sin JSON válido");
            }
        }
        
        if (preguntas.size() != evaluaciones.size() && !evaluaciones.isEmpty()) {
            problemas.add("Número de preguntas (" + preguntas.size() + 
                         ") no coincide con número de evaluaciones (" + evaluaciones.size() + ")");
        }
        
        return problemas;
    }

    private List<String> generarSugerenciasReparacion(List<Pregunta> preguntas, List<Evaluacion> evaluaciones) {
        List<String> sugerencias = new ArrayList<>();
        
        if (evaluaciones.isEmpty() && !preguntas.isEmpty()) {
            sugerencias.add("Ejecutar nuevamente las evaluaciones para todas las preguntas");
        }
        
        if (preguntas.size() > evaluaciones.size()) {
            sugerencias.add("Evaluar las preguntas faltantes");
        }
        
        long evaluacionesIncompletas = evaluaciones.stream()
            .filter(e -> e.getClaridadEstructura() == null || e.getDominioTecnico() == null)
            .count();
        
        if (evaluacionesIncompletas > 0) {
            sugerencias.add("Regenerar evaluaciones incompletas");
        }
        
        return sugerencias;
    }

    private double calcularPorcentajeSalud(long totalPostulaciones, int problemas) {
        if (totalPostulaciones == 0) return 100.0;
        return ((double)(totalPostulaciones - problemas) / totalPostulaciones) * 100.0;
    }

    private int repararEvaluacionesConDatosNulos(Long postulacionId) {
        // Implementar lógica de reparación específica
        // Por ahora retorna 0, pero se puede expandir según necesidades
        return 0;
    }

    private int repararEvaluacionesSinJSON(Long postulacionId) {
        // Implementar lógica de reparación específica
        // Por ahora retorna 0, pero se puede expandir según necesidades
        return 0;
    }
}

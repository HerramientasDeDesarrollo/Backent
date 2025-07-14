package com.example.entrevista.service;

import com.example.entrevista.model.Evaluacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.PreguntaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class ResultadosService {

    private static final Logger logger = LoggerFactory.getLogger(ResultadosService.class);

    @Autowired
    private EvaluacionService evaluacionService;
    
    @Autowired
    private DatosEvaluacionService datosEvaluacionService;
    
    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Obtiene un resumen completo de los resultados de una postulación
     * Versión optimizada usando DatosEvaluacionService
     */
    public Map<String, Object> obtenerResumenResultados(Long postulacionId) {
        try {
            logger.info("Iniciando obtención de resumen de resultados para postulación {}", postulacionId);
            
            // Usar el nuevo servicio optimizado
            DatosEvaluacionService.DatosPostulacion datos = datosEvaluacionService.obtenerDatosCompletos(postulacionId);
            
            if (!datos.isDatosCompletos()) {
                logger.warn("Datos no válidos para postulación {}: {}", postulacionId, datos.getProblemasDetectados());
                Map<String, Object> respuesta = crearRespuestaError("Problemas de integridad detectados");
                respuesta.put("problemas", datos.getProblemasDetectados());
                respuesta.put("puede_generar_resultados", false);
                return respuesta;
            }

            // Los datos ya están validados y son completos
            Postulacion postulacion = datos.getPostulacion();
            List<Evaluacion> evaluaciones = datos.getEvaluaciones();
            List<Pregunta> preguntas = datos.getPreguntas();
            
            logger.debug("Postulación {}: {} preguntas, {} evaluaciones encontradas", 
                        postulacionId, preguntas.size(), evaluaciones.size());

            if (evaluaciones.isEmpty()) {
                logger.warn("No se encontraron evaluaciones para postulación {}", postulacionId);
                Map<String, Object> respuesta = crearRespuestaSinEvaluaciones(postulacion);
                respuesta.put("estadisticas", datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId));
                return respuesta;
            }

            Map<String, Object> resultado = construirResumenCompleto(postulacion, evaluaciones);
            resultado.put("success", true);
            resultado.put("datos_completos", true);
            resultado.put("estadisticas", datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId));
            
            logger.info("Resumen de resultados generado exitosamente para postulación {}", postulacionId);
            return resultado;

        } catch (Exception e) {
            logger.error("Error al obtener resumen de resultados para postulación {}: {}", postulacionId, e.getMessage(), e);
            return crearRespuestaError("Error interno al procesar la solicitud: " + e.getMessage());
        }
    }

    /**
     * Obtiene el detalle completo de los resultados de una postulación
     * Versión optimizada usando DatosEvaluacionService
     */
    public Map<String, Object> obtenerDetalleResultados(Long postulacionId) {
        try {
            logger.info("Iniciando obtención de detalle de resultados para postulación {}", postulacionId);
            
            // Usar el nuevo servicio optimizado
            DatosEvaluacionService.DatosPostulacion datos = datosEvaluacionService.obtenerDatosCompletos(postulacionId);
            
            if (!datos.isDatosCompletos()) {
                logger.warn("Datos no válidos para postulación {}: {}", postulacionId, datos.getProblemasDetectados());
                Map<String, Object> respuesta = crearRespuestaError("Problemas de integridad detectados");
                respuesta.put("problemas", datos.getProblemasDetectados());
                return respuesta;
            }

            // Los datos ya están validados y son completos
            Postulacion postulacion = datos.getPostulacion();
            List<Evaluacion> evaluaciones = datos.getEvaluaciones();
            List<Pregunta> preguntas = datos.getPreguntas();
            
            logger.debug("Postulación {}: {} preguntas, {} evaluaciones encontradas para detalle", 
                        postulacionId, preguntas.size(), evaluaciones.size());

            if (evaluaciones.isEmpty()) {
                logger.warn("No se encontraron evaluaciones para postulación {} en detalle", postulacionId);
                Map<String, Object> respuesta = crearRespuestaSinEvaluacionesDetalle(postulacion);
                respuesta.put("estadisticas", datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId));
                return respuesta;
            }

            Map<String, Object> resultado = construirDetalleCompleto(postulacion, evaluaciones, preguntas);
            resultado.put("success", true);
            resultado.put("datos_completos", true);
            resultado.put("estadisticas", datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId));
            
            logger.info("Detalle de resultados generado exitosamente para postulación {}", postulacionId);
            return resultado;

        } catch (Exception e) {
            logger.error("Error al obtener detalle de resultados para postulación {}: {}", postulacionId, e.getMessage(), e);
            return crearRespuestaError("Error interno al procesar la solicitud: " + e.getMessage());
        }
    }

    /**
     * Obtiene resultados organizados por postulación para empresas
     */
    public Map<String, Object> obtenerResultadosPorEntrevista(Long entrevistaId) {
        try {
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorEntrevista(entrevistaId);
            
            if (evaluaciones.isEmpty()) {
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("success", true);
                respuesta.put("mensaje", "No se encontraron evaluaciones para este entrevistador");
                respuesta.put("evaluaciones", evaluaciones);
                return respuesta;
            }

            // Agrupar evaluaciones por postulación
            Map<Long, List<Evaluacion>> evaluacionesPorPostulacion = evaluaciones.stream()
                .filter(e -> e.getPostulacion() != null)
                .collect(Collectors.groupingBy(e -> e.getPostulacion().getId()));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Resultados encontrados");
            respuesta.put("totalPostulaciones", evaluacionesPorPostulacion.size());
            respuesta.put("totalEvaluaciones", evaluaciones.size());
            respuesta.put("resultadosPorPostulacion", evaluacionesPorPostulacion);
            return respuesta;

        } catch (Exception e) {
            logger.error("Error al obtener resultados para entrevistador {}: {}", entrevistaId, e.getMessage(), e);
            return crearRespuestaError("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    /**
     * Verifica la integridad de los datos antes de procesar resultados
     */
    public Map<String, Object> verificarIntegridadDatos(Long postulacionId) {
        Map<String, Object> verificacion = new HashMap<>();
        
        try {
            // Verificar postulación
            Optional<Postulacion> postulacionOpt = postulacionRepository.findById(postulacionId);
            if (!postulacionOpt.isPresent()) {
                verificacion.put("valida", false);
                verificacion.put("error", "Postulación no encontrada");
                return verificacion;
            }

            Postulacion postulacion = postulacionOpt.get();
            List<Evaluacion> evaluaciones = evaluacionService.obtenerEvaluacionesPorPostulacion(postulacionId);
            List<Pregunta> preguntas = preguntaRepository.findByPostulacionId(postulacionId);

            // Verificaciones básicas
            boolean integridadOk = true;
            List<String> advertencias = new ArrayList<>();

            if (preguntas.isEmpty()) {
                advertencias.add("No hay preguntas asociadas");
                integridadOk = false;
            }

            if (evaluaciones.isEmpty()) {
                advertencias.add("No hay evaluaciones registradas");
                integridadOk = false;
            }

            if (!preguntas.isEmpty() && !evaluaciones.isEmpty() && preguntas.size() != evaluaciones.size()) {
                advertencias.add("Número de preguntas y evaluaciones no coincide");
                integridadOk = false;
            }

            // Verificar calidad de evaluaciones
            long evaluacionesIncompletas = evaluaciones.stream()
                .filter(e -> e.getClaridadEstructura() == null || e.getDominioTecnico() == null ||
                           e.getPertinencia() == null || e.getComunicacionSeguridad() == null ||
                           e.getPorcentajeObtenido() == null)
                .count();

            if (evaluacionesIncompletas > 0) {
                advertencias.add("Hay " + evaluacionesIncompletas + " evaluaciones con datos incompletos");
                integridadOk = false;
            }

            verificacion.put("valida", integridadOk);
            verificacion.put("total_preguntas", preguntas.size());
            verificacion.put("total_evaluaciones", evaluaciones.size());
            verificacion.put("evaluaciones_incompletas", evaluacionesIncompletas);
            verificacion.put("advertencias", advertencias);
            
            Map<String, Object> postulacionInfo = new HashMap<>();
            postulacionInfo.put("id", postulacion.getId());
            postulacionInfo.put("usuario_id", postulacion.getUsuario().getId());
            postulacionInfo.put("convocatoria_id", postulacion.getConvocatoria().getId());
            verificacion.put("postulacion", postulacionInfo);

        } catch (Exception e) {
            logger.error("Error verificando integridad para postulación {}: {}", postulacionId, e.getMessage(), e);
            verificacion.put("valida", false);
            verificacion.put("error", "Error durante verificación: " + e.getMessage());
        }

        return verificacion;
    }

    /**
     * Validación robusta para garantizar que los datos estén listos para mostrar resultados
     */
    private boolean validarDatosParaResultados(Long postulacionId, List<Evaluacion> evaluaciones, List<Pregunta> preguntas) {
        // Validación 1: Debe haber preguntas
        if (preguntas == null || preguntas.isEmpty()) {
            logger.warn("Postulación {} no tiene preguntas asociadas", postulacionId);
            return false;
        }

        // Validación 2: Debe haber evaluaciones
        if (evaluaciones == null || evaluaciones.isEmpty()) {
            logger.warn("Postulación {} no tiene evaluaciones registradas", postulacionId);
            return false;
        }

        // Validación 3: Número de evaluaciones debe coincidir con preguntas
        if (evaluaciones.size() != preguntas.size()) {
            logger.warn("Postulación {} tiene {} preguntas pero {} evaluaciones", 
                       postulacionId, preguntas.size(), evaluaciones.size());
            return false;
        }

        // Validación 4: Cada evaluación debe tener datos completos
        long evaluacionesIncompletas = evaluaciones.stream()
            .filter(e -> e.getClaridadEstructura() == null || 
                        e.getDominioTecnico() == null || 
                        e.getPertinencia() == null || 
                        e.getComunicacionSeguridad() == null ||
                        e.getPorcentajeObtenido() == null)
            .count();

        if (evaluacionesIncompletas > 0) {
            logger.warn("Postulación {} tiene {} evaluaciones con datos incompletos", 
                       postulacionId, evaluacionesIncompletas);
            return false;
        }

        // Validación 5: Cada evaluación debe tener JSON válido para fortalezas/oportunidades
        long evaluacionesSinJSON = evaluaciones.stream()
            .filter(e -> e.getEvaluacionCompleta() == null || 
                        e.getEvaluacionCompleta().trim().isEmpty())
            .count();

        if (evaluacionesSinJSON > 0) {
            logger.warn("Postulación {} tiene {} evaluaciones sin JSON válido", 
                       postulacionId, evaluacionesSinJSON);
            return false;
        }

        // Validación 6: Verificar que cada pregunta tenga su evaluación correspondiente
        Set<Long> preguntaIds = preguntas.stream()
            .map(Pregunta::getId)
            .collect(Collectors.toSet());

        Set<Long> evaluacionPreguntaIds = evaluaciones.stream()
            .filter(e -> e.getPregunta() != null)
            .map(e -> e.getPregunta().getId())
            .collect(Collectors.toSet());

        if (!preguntaIds.equals(evaluacionPreguntaIds)) {
            logger.warn("Postulación {} tiene desalineación entre preguntas y evaluaciones. " +
                       "Preguntas IDs: {}, Evaluaciones IDs: {}", 
                       postulacionId, preguntaIds, evaluacionPreguntaIds);
            return false;
        }

        logger.info("Postulación {} pasó todas las validaciones de datos", postulacionId);
        return true;
    }

    // Métodos privados para construcción de respuestas

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", false);
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }

    private Map<String, Object> crearRespuestaSinEvaluaciones(Postulacion postulacion) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("usuarioId", postulacion.getUsuario().getId());
        respuesta.put("convocatoriaId", postulacion.getConvocatoria().getId());
        respuesta.put("tituloConvocatoria", postulacion.getConvocatoria().getJobTitle());
        respuesta.put("mensaje", "No se encontraron evaluaciones para esta postulación");
        return respuesta;
    }

    private Map<String, Object> crearRespuestaSinEvaluacionesDetalle(Postulacion postulacion) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("usuarioId", postulacion.getUsuario().getId());
        respuesta.put("convocatoriaId", postulacion.getConvocatoria().getId());
        respuesta.put("tituloConvocatoria", postulacion.getConvocatoria().getJobTitle());
        respuesta.put("mensaje", "No se encontraron evaluaciones para esta postulación");
        respuesta.put("detallePreguntas", Collections.emptyList());
        return respuesta;
    }

    private Map<String, Object> construirResumenCompleto(Postulacion postulacion, List<Evaluacion> evaluaciones) {
        // Calcular promedios por criterio de manera segura
        PromediosCriterios promedios = calcularPromediosCriterios(evaluaciones);
        
        // Calcular puntaje final
        double puntajeFinal = calcularPuntajeFinal(evaluaciones);
        
        // Extraer fortalezas y oportunidades
        FortalezasOportunidades fortalezasOportunidades = extraerFortalezasYOportunidades(evaluaciones);
        
        // Obtener fecha más reciente
        Date fechaEvaluacion = obtenerFechaMasReciente(evaluaciones);
        
        Map<String, Object> resultados = new HashMap<>();
        resultados.put("success", true);
        resultados.put("usuarioId", postulacion.getUsuario().getId());
        resultados.put("convocatoriaId", postulacion.getConvocatoria().getId());
        resultados.put("tituloConvocatoria", postulacion.getConvocatoria().getJobTitle());
        resultados.put("fechaEvaluacion", fechaEvaluacion);
        resultados.put("puntajeFinal", redondear(puntajeFinal));
        
        Map<String, Object> resumenPorCriterio = new HashMap<>();
        resumenPorCriterio.put("claridad_estructura", promedios.promedioClaridadEstructura);
        resumenPorCriterio.put("dominio_tecnico", promedios.promedioDominioTecnico);
        resumenPorCriterio.put("pertinencia", promedios.promedioPertinencia);
        resumenPorCriterio.put("comunicacion_seguridad", promedios.promedioComunicacionSeguridad);
        
        resultados.put("resumenPorCriterio", resumenPorCriterio);
        resultados.put("fortalezas", fortalezasOportunidades.fortalezas);
        resultados.put("oportunidadesMejora", fortalezasOportunidades.oportunidades);
        
        return resultados;
    }

    private Map<String, Object> construirDetalleCompleto(Postulacion postulacion, List<Evaluacion> evaluaciones, List<Pregunta> preguntas) {
        // Crear mapa de evaluaciones por pregunta
        Map<Long, Evaluacion> evaluacionesPorPregunta = evaluaciones.stream()
            .filter(e -> e.getPregunta() != null)
            .collect(Collectors.toMap(
                e -> e.getPregunta().getId(),
                e -> e,
                (existing, replacement) -> replacement // Mantener la evaluación más reciente
            ));

        // Construir detalle de preguntas
        List<Map<String, Object>> detallePreguntas = construirDetallePreguntasConEvaluaciones(preguntas, evaluacionesPorPregunta);
        
        Map<String, Object> resultados = new HashMap<>();
        resultados.put("success", true);
        resultados.put("usuarioId", postulacion.getUsuario().getId());
        resultados.put("convocatoriaId", postulacion.getConvocatoria().getId());
        resultados.put("tituloConvocatoria", postulacion.getConvocatoria().getJobTitle());
        resultados.put("detallePreguntas", detallePreguntas);
        
        return resultados;
    }

    private List<Map<String, Object>> construirDetallePreguntasConEvaluaciones(List<Pregunta> preguntas, Map<Long, Evaluacion> evaluacionesPorPregunta) {
        List<Map<String, Object>> detallePreguntas = new ArrayList<>();
        
        for (Pregunta pregunta : preguntas) {
            Map<String, Object> detallePregunta = new HashMap<>();
            detallePregunta.put("numero", pregunta.getNumero());
            detallePregunta.put("tipo", pregunta.getTipoLegible());
            detallePregunta.put("pregunta", pregunta.getTextoPregunta());
            
            Evaluacion evaluacion = evaluacionesPorPregunta.get(pregunta.getId());
            
            if (evaluacion != null) {
                detallePregunta.put("respuestaUsuario", evaluacion.getRespuesta());
                detallePregunta.put("evaluacion", construirDetalleEvaluacion(evaluacion));
            } else {
                detallePregunta.put("respuestaUsuario", "No se ha registrado respuesta");
                detallePregunta.put("evaluacion", null);
            }
            
            detallePreguntas.add(detallePregunta);
        }
        
        // Ordenar por número de pregunta
        detallePreguntas.sort(Comparator.comparing(m -> (Integer) m.get("numero")));
        
        return detallePreguntas;
    }

    private Map<String, Object> construirDetalleEvaluacion(Evaluacion evaluacion) {
        Map<String, Object> detalleEvaluacion = new HashMap<>();
        detalleEvaluacion.put("claridad_estructura", evaluacion.getClaridadEstructura());
        detalleEvaluacion.put("dominio_tecnico", evaluacion.getDominioTecnico());
        detalleEvaluacion.put("pertinencia", evaluacion.getPertinencia());
        detalleEvaluacion.put("comunicacion_seguridad", evaluacion.getComunicacionSeguridad());
        detalleEvaluacion.put("puntuacion_final", evaluacion.getPorcentajeObtenido());
        
        // Extraer fortalezas y oportunidades del JSON
        FortalezasOportunidades fortalezasOportunidades = extraerFortalezasYOportunidadesDeEvaluacion(evaluacion);
        detalleEvaluacion.put("fortalezas", fortalezasOportunidades.fortalezas);
        detalleEvaluacion.put("oportunidades_mejora", fortalezasOportunidades.oportunidades);
        
        return detalleEvaluacion;
    }

    // Clases auxiliares para mejor organización
    private static class PromediosCriterios {
        double promedioClaridadEstructura;
        double promedioDominioTecnico;
        double promedioPertinencia;
        double promedioComunicacionSeguridad;
        
        PromediosCriterios(double claridad, double dominio, double pertinencia, double comunicacion) {
            this.promedioClaridadEstructura = redondear(claridad);
            this.promedioDominioTecnico = redondear(dominio);
            this.promedioPertinencia = redondear(pertinencia);
            this.promedioComunicacionSeguridad = redondear(comunicacion);
        }
    }

    private static class FortalezasOportunidades {
        List<String> fortalezas;
        List<String> oportunidades;
        
        FortalezasOportunidades(List<String> fortalezas, List<String> oportunidades) {
            this.fortalezas = fortalezas != null ? fortalezas : Collections.emptyList();
            this.oportunidades = oportunidades != null ? oportunidades : Collections.emptyList();
        }
    }

    // Métodos de cálculo mejorados
    private PromediosCriterios calcularPromediosCriterios(List<Evaluacion> evaluaciones) {
        double promedioClaridad = evaluaciones.stream()
            .mapToDouble(e -> e.getClaridadEstructura() != null ? e.getClaridadEstructura() : 0)
            .average().orElse(0);
            
        double promedioDominio = evaluaciones.stream()
            .mapToDouble(e -> e.getDominioTecnico() != null ? e.getDominioTecnico() : 0)
            .average().orElse(0);
            
        double promedioPertinencia = evaluaciones.stream()
            .mapToDouble(e -> e.getPertinencia() != null ? e.getPertinencia() : 0)
            .average().orElse(0);
            
        double promedioComunicacion = evaluaciones.stream()
            .mapToDouble(e -> e.getComunicacionSeguridad() != null ? e.getComunicacionSeguridad() : 0)
            .average().orElse(0);

        return new PromediosCriterios(promedioClaridad, promedioDominio, promedioPertinencia, promedioComunicacion);
    }

    private double calcularPuntajeFinal(List<Evaluacion> evaluaciones) {
        return evaluaciones.stream()
            .mapToDouble(e -> e.getPorcentajeObtenido() != null ? e.getPorcentajeObtenido() : 0)
            .sum();
    }

    private FortalezasOportunidades extraerFortalezasYOportunidades(List<Evaluacion> evaluaciones) {
        Set<String> fortalezasSet = new HashSet<>();
        Set<String> oportunidadesSet = new HashSet<>();
        
        for (Evaluacion evaluacion : evaluaciones) {
            FortalezasOportunidades fo = extraerFortalezasYOportunidadesDeEvaluacion(evaluacion);
            fortalezasSet.addAll(fo.fortalezas);
            oportunidadesSet.addAll(fo.oportunidades);
        }
        
        return new FortalezasOportunidades(new ArrayList<>(fortalezasSet), new ArrayList<>(oportunidadesSet));
    }

    private FortalezasOportunidades extraerFortalezasYOportunidadesDeEvaluacion(Evaluacion evaluacion) {
        List<String> fortalezas = Collections.emptyList();
        List<String> oportunidades = Collections.emptyList();
        
        if (evaluacion.getEvaluacionCompleta() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> evaluacionObj = objectMapper.readValue(evaluacion.getEvaluacionCompleta(), Map.class);
                
                if (evaluacionObj.containsKey("fortalezas") && evaluacionObj.get("fortalezas") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> fortList = (List<String>) evaluacionObj.get("fortalezas");
                    fortalezas = fortList != null ? fortList : Collections.emptyList();
                }
                
                if (evaluacionObj.containsKey("oportunidades_mejora") && evaluacionObj.get("oportunidades_mejora") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> opList = (List<String>) evaluacionObj.get("oportunidades_mejora");
                    oportunidades = opList != null ? opList : Collections.emptyList();
                }
            } catch (Exception e) {
                logger.warn("Error al parsear evaluación JSON para evaluación {}: {}", evaluacion.getId(), e.getMessage());
            }
        }
        
        return new FortalezasOportunidades(fortalezas, oportunidades);
    }

    private Date obtenerFechaMasReciente(List<Evaluacion> evaluaciones) {
        return evaluaciones.stream()
            .map(Evaluacion::getFechaEvaluacion)
            .filter(Objects::nonNull)
            .max(Date::compareTo)
            .orElse(new Date());
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // Métodos de conveniencia para usar el nuevo DatosEvaluacionService
    
    /**
     * Verificación rápida si una postulación puede generar resultados
     */
    public boolean puedeGenerarResultados(Long postulacionId) {
        return datosEvaluacionService.puedeGenerarResultados(postulacionId);
    }
    
    /**
     * Obtiene estadísticas rápidas sin cargar todos los datos
     */
    public Map<String, Object> obtenerEstadisticasRapidas(Long postulacionId) {
        return datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId);
    }
    
    /**
     * Obtiene evaluaciones validadas usando el nuevo servicio
     */
    public List<Evaluacion> obtenerEvaluacionesValidadas(Long postulacionId) {
        return datosEvaluacionService.obtenerEvaluacionesValidadas(postulacionId);
    }
}

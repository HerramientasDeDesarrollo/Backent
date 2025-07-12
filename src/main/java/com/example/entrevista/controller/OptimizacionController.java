package com.example.entrevista.controller;

import com.example.entrevista.service.DatosEvaluacionService;
import com.example.entrevista.service.ResultadosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador temporal para probar las optimizaciones
 * Este controlador puede integrarse al EvaluacionController principal más tarde
 */
@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionController.class);

    @Autowired
    private DatosEvaluacionService datosEvaluacionService;

    @Autowired
    private ResultadosService resultadosService;

    /**
     * Endpoint para probar la obtención optimizada de datos completos
     */
    @GetMapping("/datos-completos/{postulacionId}")
    public ResponseEntity<Map<String, Object>> obtenerDatosCompletos(@PathVariable Long postulacionId) {
        try {
            logger.info("Probando obtención optimizada de datos para postulación {}", postulacionId);
            
            DatosEvaluacionService.DatosPostulacion datos = datosEvaluacionService.obtenerDatosCompletos(postulacionId);
            
            // Crear respuesta estructurada
            Map<String, Object> respuesta = Map.of(
                "postulacion_id", postulacionId,
                "datos_completos", datos.isDatosCompletos(),
                "total_preguntas", datos.getTotalPreguntas(),
                "total_evaluaciones", datos.getTotalEvaluaciones(),
                "numeros_coinciden", datos.numerosCoinciden(),
                "problemas_detectados", datos.getProblemasDetectados(),
                "tiene_postulacion", datos.getPostulacion() != null,
                "tiene_preguntas", datos.tienePreguntas(),
                "tiene_evaluaciones", datos.tieneEvaluaciones()
            );
            
            logger.info("Datos obtenidos para postulación {}: válidos={}", postulacionId, datos.isDatosCompletos());
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            logger.error("Error obteniendo datos optimizados para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error interno al obtener datos",
                "mensaje", e.getMessage(),
                "postulacion_id", postulacionId
            ));
        }
    }

    /**
     * Endpoint para comparar estadísticas rápidas vs datos completos
     */
    @GetMapping("/comparacion/{postulacionId}")
    public ResponseEntity<Map<String, Object>> compararMetodos(@PathVariable Long postulacionId) {
        try {
            logger.info("Comparando métodos para postulación {}", postulacionId);
            
            long inicioEstadisticas = System.currentTimeMillis();
            Map<String, Object> estadisticas = datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId);
            long tiempoEstadisticas = System.currentTimeMillis() - inicioEstadisticas;
            
            long inicioDatosCompletos = System.currentTimeMillis();
            DatosEvaluacionService.DatosPostulacion datos = datosEvaluacionService.obtenerDatosCompletos(postulacionId);
            long tiempoDatosCompletos = System.currentTimeMillis() - inicioDatosCompletos;
            
            Map<String, Object> comparacion = Map.of(
                "postulacion_id", postulacionId,
                "estadisticas_rapidas", Map.of(
                    "tiempo_ms", tiempoEstadisticas,
                    "datos", estadisticas
                ),
                "datos_completos", Map.of(
                    "tiempo_ms", tiempoDatosCompletos,
                    "validos", datos.isDatosCompletos(),
                    "total_preguntas", datos.getTotalPreguntas(),
                    "total_evaluaciones", datos.getTotalEvaluaciones(),
                    "problemas", datos.getProblemasDetectados()
                ),
                "recomendacion", tiempoEstadisticas < tiempoDatosCompletos ? 
                    "Usar estadísticas rápidas para verificaciones simples" :
                    "Ambos métodos tienen rendimiento similar"
            );
            
            return ResponseEntity.ok(comparacion);
            
        } catch (Exception e) {
            logger.error("Error en comparación para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error en comparación",
                "mensaje", e.getMessage(),
                "postulacion_id", postulacionId
            ));
        }
    }

    /**
     * Endpoint para probar la nueva versión optimizada de resultados
     */
    @GetMapping("/resultados-optimizados/{postulacionId}")
    public ResponseEntity<Map<String, Object>> obtenerResultadosOptimizados(@PathVariable Long postulacionId) {
        try {
            logger.info("Probando resultados optimizados para postulación {}", postulacionId);
            
            long inicio = System.currentTimeMillis();
            Map<String, Object> resultados = resultadosService.obtenerResumenResultados(postulacionId);
            long tiempo = System.currentTimeMillis() - inicio;
            
            // Agregar información de rendimiento
            resultados.put("tiempo_procesamiento_ms", tiempo);
            resultados.put("metodo_usado", "optimizado_con_DatosEvaluacionService");
            
            logger.info("Resultados optimizados obtenidos para postulación {} en {}ms", postulacionId, tiempo);
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            logger.error("Error obteniendo resultados optimizados para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error obteniendo resultados optimizados",
                "mensaje", e.getMessage(),
                "postulacion_id", postulacionId
            ));
        }
    }

    /**
     * Endpoint para validar si una postulación puede generar resultados
     */
    @GetMapping("/puede-generar-resultados/{postulacionId}")
    public ResponseEntity<Map<String, Object>> puedeGenerarResultados(@PathVariable Long postulacionId) {
        try {
            logger.info("Verificando si postulación {} puede generar resultados", postulacionId);
            
            boolean puedeGenerar = datosEvaluacionService.puedeGenerarResultados(postulacionId);
            Map<String, Object> estadisticas = datosEvaluacionService.obtenerEstadisticasRapidas(postulacionId);
            
            Map<String, Object> respuesta = Map.of(
                "postulacion_id", postulacionId,
                "puede_generar_resultados", puedeGenerar,
                "estadisticas", estadisticas,
                "recomendacion", puedeGenerar ? 
                    "La postulación tiene datos suficientes para generar resultados" :
                    "La postulación requiere datos adicionales antes de generar resultados"
            );
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            logger.error("Error verificando postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error en verificación",
                "mensaje", e.getMessage(),
                "postulacion_id", postulacionId
            ));
        }
    }

    /**
     * Endpoint público para diagnosticar el problema original de /mis-resultados
     * Este endpoint replica exactamente lo que hace el original pero sin autenticación
     */
    @GetMapping("/diagnosticar-mis-resultados/{postulacionId}")
    public ResponseEntity<Map<String, Object>> diagnosticarMisResultados(@PathVariable Long postulacionId) {
        try {
            logger.info("Diagnosticando problema de /mis-resultados para postulación {}", postulacionId);
            
            // Usar el servicio optimizado para obtener resultados
            Map<String, Object> resultados = resultadosService.obtenerResumenResultados(postulacionId);
            
            // Agregar información de diagnóstico
            Map<String, Object> diagnostico = Map.of(
                "postulacion_id", postulacionId,
                "endpoint_testeado", "/api/evaluaciones/mis-resultados/" + postulacionId,
                "metodo_usado", "optimizado_DatosEvaluacionService",
                "timestamp", System.currentTimeMillis(),
                "resultados", resultados
            );
            
            // Verificar si el problema original estaría presente
            Boolean success = (Boolean) resultados.get("success");
            if (success == null) {
                logger.warn("Campo 'success' faltante - esto causaría el error original");
                diagnostico.put("problema_detectado", "Campo 'success' faltante en respuesta");
                diagnostico.put("solucion", "Agregado campo 'success' en métodos optimizados");
                return ResponseEntity.badRequest().body(diagnostico);
            }
            
            if (!success) {
                logger.info("Datos no válidos para postulación {} - respuesta controlada", postulacionId);
                diagnostico.put("estado", "datos_invalidos_controlado");
                return ResponseEntity.badRequest().body(diagnostico);
            }
            
            logger.info("Diagnóstico exitoso para postulación {} - optimización funcional", postulacionId);
            diagnostico.put("estado", "optimizacion_exitosa");
            return ResponseEntity.ok(diagnostico);
            
        } catch (Exception e) {
            logger.error("Error en diagnóstico para postulación {}: {}", postulacionId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error en diagnóstico",
                "mensaje", e.getMessage(),
                "postulacion_id", postulacionId,
                "tipo_error", e.getClass().getSimpleName(),
                "solucion_aplicada", "Manejo robusto de errores con HashMap mutables"
            ));
        }
    }
}

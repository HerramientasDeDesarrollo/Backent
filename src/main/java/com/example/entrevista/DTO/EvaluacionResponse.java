package com.example.entrevista.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Esto evita que los campos nulos aparezcan en el JSON
public class EvaluacionResponse {
    private Long id;
    private Long preguntaId;
    private Long postulacionId;
    private String pregunta;
    private String respuesta;
    private Date fechaEvaluacion;
    private String mensaje;
    private boolean success;
    
    // Organización de resultados en un objeto anidado para mejor estructura
    @JsonProperty("evaluacion")
    private EvaluacionData evaluacion;
    
    // Clase interna para organizar los datos de evaluación
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EvaluacionData {
        // Métricas numéricas
        private Integer claridadEstructura;
        private Integer dominioTecnico;
        private Integer pertinencia;
        private Integer comunicacionSeguridad;
        private Double puntajeTotal;
        private Double porcentajeObtenido;
        
        // Comentarios cualitativos
        private List<String> fortalezas;
        private List<String> oportunidadesMejora;
        
        // JSON completo por si se necesita acceder a todos los detalles
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private Map<String, Object> detallesCompletos;
    }

    // Método estático para crear respuesta de error
    public static EvaluacionResponse error(String errorMessage) {
        EvaluacionResponse response = new EvaluacionResponse();
        response.setSuccess(false);
        response.setMensaje(errorMessage);
        return response;
    }
    
    // Método para establecer los datos completos de la evaluación de una vez
    public void setEvaluacionData(
            Integer claridadEstructura, 
            Integer dominioTecnico,
            Integer pertinencia,
            Integer comunicacionSeguridad,
            Double puntajeTotal,
            Double porcentajeObtenido,
            List<String> fortalezas,
            List<String> oportunidadesMejora,
            Map<String, Object> detallesCompletos) {
        
        EvaluacionData data = new EvaluacionData();
        data.setClaridadEstructura(claridadEstructura);
        data.setDominioTecnico(dominioTecnico);
        data.setPertinencia(pertinencia);
        data.setComunicacionSeguridad(comunicacionSeguridad);
        data.setPuntajeTotal(puntajeTotal);
        data.setPorcentajeObtenido(porcentajeObtenido);
        data.setFortalezas(fortalezas);
        data.setOportunidadesMejora(oportunidadesMejora);
        data.setDetallesCompletos(detallesCompletos);
        this.evaluacion = data;
    }
}

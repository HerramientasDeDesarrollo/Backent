package com.example.entrevista.service;

import com.example.entrevista.DTO.EvaluacionRequest;
import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.model.Evaluacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.repository.EvaluacionRepository;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.PreguntaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EvaluacionService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;

    public EvaluacionService(WebClient openAIWebClient) {
        this.webClient = openAIWebClient;
    }

    public EvaluacionResponse evaluarPregunta(EvaluacionRequest request) {
        String prompt = construirPrompt(request.getPuesto(), request.getQuestion(), request.getAnswer(), request.getValorPregunta());

        Map<String, Object> response = enviarAOpenAIConTokens(prompt);

        if (response == null || response.isEmpty()) {
            return EvaluacionResponse.error("No se pudo obtener respuesta de la API.");
        }

        // Extraer contenido JSON de la respuesta
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return EvaluacionResponse.error("Respuesta vacía de la API.");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        try {
            // Parsear el JSON de la respuesta para extraer los detalles de evaluación
            Map<String, Object> evaluacionDetallada = objectMapper.readValue(content, Map.class);
            
            // Obtener los valores numéricos de la evaluación
            Integer claridadEstructura = (Integer) evaluacionDetallada.get("claridad_estructura");
            Integer dominioTecnico = (Integer) evaluacionDetallada.get("dominio_tecnico");
            Integer pertinencia = (Integer) evaluacionDetallada.get("pertinencia");
            Integer comunicacionSeguridad = (Integer) evaluacionDetallada.get("comunicacion_seguridad");
            
            // Extraer las listas de fortalezas y oportunidades de mejora
            @SuppressWarnings("unchecked")
            List<String> fortalezas = (List<String>) evaluacionDetallada.get("fortalezas");
            @SuppressWarnings("unchecked")
            List<String> oportunidadesMejora = (List<String>) evaluacionDetallada.get("oportunidades_mejora");
            
            // Calcular puntaje total y porcentaje (con máximo 2 decimales)
            double puntajeTotal = Math.round((claridadEstructura + dominioTecnico + pertinencia + comunicacionSeguridad) / 4.0 * 100) / 100.0;
            double porcentajeObtenido = Math.round((puntajeTotal * request.getValorPregunta()) / 100.0 * 100) / 100.0;
            
            // Asegurarse que el porcentaje obtenido no supere el valor máximo de la pregunta
            porcentajeObtenido = Math.min(porcentajeObtenido, request.getValorPregunta());
            
            // Crear y configurar la respuesta
            EvaluacionResponse evaluacionResponse = new EvaluacionResponse();
            evaluacionResponse.setSuccess(true);
            evaluacionResponse.setPregunta(request.getQuestion());
            evaluacionResponse.setRespuesta(request.getAnswer());
            evaluacionResponse.setFechaEvaluacion(new Date());
            evaluacionResponse.setMensaje("Evaluación completada exitosamente");
            
            // Configurar los datos de evaluación estructurados
            evaluacionResponse.setEvaluacionData(
                claridadEstructura,
                dominioTecnico,
                pertinencia,
                comunicacionSeguridad,
                puntajeTotal,
                porcentajeObtenido,
                fortalezas,
                oportunidadesMejora,
                evaluacionDetallada
            );
            
            if (request.getIdPostulacion() != null) {
                evaluacionResponse.setPostulacionId(request.getIdPostulacion());
            }

            // Crear y guardar la entidad Evaluacion
            Evaluacion evaluacion = new Evaluacion();
            evaluacion.setFechaEvaluacion(new Date());
            evaluacion.setEvaluacionCompleta(content);
            evaluacion.setRespuesta(request.getAnswer());
            evaluacion.setEstado("completada");
            
            // Guardar las métricas de evaluación
            evaluacion.setClaridadEstructura(claridadEstructura);
            evaluacion.setDominioTecnico(dominioTecnico);
            evaluacion.setPertinencia(pertinencia);
            evaluacion.setComunicacionSeguridad(comunicacionSeguridad);
            evaluacion.setPuntajeTotal(puntajeTotal);
            evaluacion.setPorcentajeObtenido(porcentajeObtenido);

            // Buscar y establecer la relación con la Pregunta
            Long preguntaId = null;
            
            // Buscar la pregunta por su texto si no tenemos el ID explícito
            List<Pregunta> preguntas = preguntaRepository.findByTextoPregunta(request.getQuestion());
            if (!preguntas.isEmpty()) {
                Pregunta pregunta = preguntas.get(0);
                preguntaId = pregunta.getId();
                evaluacion.setPregunta(pregunta);
                evaluacionResponse.setPreguntaId(preguntaId);
            }

            // Establecer la relación con la Postulación
            if (request.getIdPostulacion() != null) {
                Optional<Postulacion> postulacionOpt = postulacionRepository.findById(request.getIdPostulacion());
                postulacionOpt.ifPresent(evaluacion::setPostulacion);
            }

            // Guardar la evaluación en la base de datos
            Evaluacion evaluacionGuardada = evaluacionRepository.save(evaluacion);
            evaluacionResponse.setId(evaluacionGuardada.getId());

            return evaluacionResponse;

        } catch (Exception e) {
            return EvaluacionResponse.error("Error al parsear la respuesta JSON: " + e.getMessage());
        }
    }

    private String construirPrompt(String puesto, String pregunta, String respuesta, int valorPregunta) {
        return """
            Eres un evaluador ESTRICTO y EXIGENTE de entrevistas laborales con más de 15 años de experiencia. Debes ser RIGUROSO en tu evaluación.

            Evalúa la siguiente respuesta de un candidato al puesto de "%s":

            Pregunta: %s
            Respuesta: %s
            Valor de la pregunta: %d puntos (Este es el puntaje máximo posible para esta pregunta)

            INSTRUCCIONES CRÍTICAS:
            - SÉ ESTRICTO: No seas benevolente. Evalúa con criterios profesionales altos.
            - IDENTIFICA DEBILIDADES: Busca activamente problemas, vacíos de conocimiento, falta de ejemplos concretos.
            - EXIGE PROFUNDIDAD: Una respuesta superficial debe ser penalizada severamente.
            - NO PREMIES RESPUESTAS GENÉRICAS: Las respuestas vagas o sin ejemplos específicos deben tener puntuaciones bajas.

            Analiza la respuesta según estos criterios (valores entre 1 y 100):
            - claridad_estructura: ¿La respuesta está bien organizada y es fácil de seguir? (Penaliza si es confusa o desordenada)
            - dominio_tecnico: ¿Demuestra conocimiento profundo y correcto? (Penaliza respuestas superficiales o incorrectas)
            - pertinencia: ¿Responde directamente la pregunta sin divagar? (Penaliza si se va por las ramas)
            - comunicacion_seguridad: ¿Se expresa con confianza y profesionalismo? (Penaliza dudas excesivas o falta de fluidez)

            Para fortalezas y oportunidades_mejora:
            - OBLIGATORIO: Siempre incluir MÍNIMO 2 fortalezas Y MÍNIMO 3 oportunidades de mejora
            - Las fortalezas deben ser específicas y justificadas
            - Las oportunidades de mejora deben ser CONCRETAS y ACCIONABLES
            - Si la respuesta es mala, sé más específico en las críticas

            IMPORTANTE: El puntaje final se calculará como un porcentaje del valor de la pregunta (%d puntos).
            Tu calificación debe ser justa y nunca exceder el valor máximo de la pregunta.

            Responde ÚNICAMENTE con este formato JSON:
            {
              "claridad_estructura": 0,
              "dominio_tecnico": 0,
              "pertinencia": 0,
              "comunicacion_seguridad": 0,
              "fortalezas": ["fortaleza específica 1", "fortaleza específica 2"],
              "oportunidades_mejora": ["mejora concreta 1", "mejora concreta 2", "mejora concreta 3"]
            }
            """.formatted(puesto, pregunta, respuesta, valorPregunta, valorPregunta);
    }

    private Map<String, Object> enviarAOpenAIConTokens(String prompt) {
        try {
            // Configuración de la solicitud
            Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
            );

            // Envía la solicitud a la API de OpenAI
            Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            return response;
        } catch (Exception e) {
            // Log the error
            System.err.println("Error al llamar a la API de OpenAI: " + e.getMessage());
            // Return an empty map or throw a more specific exception
            return Map.of("error", e.getMessage());
        }
    }
    
    // Métodos adicionales para consultar resultados
    public List<Evaluacion> obtenerEvaluacionesPorPostulacion(Long postulacionId) {
        return evaluacionRepository.findByPostulacionId(postulacionId);
    }
    
    public List<Evaluacion> obtenerEvaluacionesPorPregunta(Long preguntaId) {
        return evaluacionRepository.findByPreguntaId(preguntaId);
    }
}

package com.example.entrevista.service;

import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.model.Evaluacion;
import com.example.entrevista.repository.EvaluationRepository;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.DTO.EvaluacionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class EvaluacionService {

    private final WebClient webClient;
    private static final double COST_PER_1K_TOKENS = 0.002; // gpt-3.5-turbo

    @Autowired
    private EvaluationRepository evaluacionRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;

    public EvaluacionService(WebClient openAIWebClient) {
        this.webClient = openAIWebClient;
    }

    /**
     * Evalúa una sola pregunta y respuesta
     */
    public EvaluacionResponse evaluarPregunta(EvaluacionRequest request) {
        String prompt = construirPrompt(request.getPuesto(), request.getQuestion(), request.getAnswer(), request.getValorPregunta());

        Map<String, Object> response = enviarAOpenAIConTokens(prompt);

        if (response == null || response.isEmpty()) {
            return EvaluacionResponse.error("No se pudo obtener respuesta de la API.");
        }

        // Extraer información de tokens y costo
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        int promptTokens = 0;
        int completionTokens = 0;
        int totalTokens = 0;
        double costoUSD = 0.0;
        double costoSoles = 0.0;

        if (usage != null) {
            promptTokens = (Integer) usage.getOrDefault("prompt_tokens", 0);
            completionTokens = (Integer) usage.getOrDefault("completion_tokens", 0);
            totalTokens = (Integer) usage.getOrDefault("total_tokens", 0);
            costoUSD = (totalTokens / 1000.0) * COST_PER_1K_TOKENS;
            costoSoles = costoUSD * 3.64; // Asumiendo 1 USD = 3.64 PEN

            System.out.println("Tokens usados:");
            System.out.println(" - Prompt: " + promptTokens);
            System.out.println(" - Respuesta: " + completionTokens);
            System.out.println(" - Total: " + totalTokens);
            System.out.printf("Costo aproximado: $%.6f USD%n", costoUSD);
            System.out.println("Costo a soles: S/ " + costoSoles);
        }

        // Extraer contenido JSON de la respuesta
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return EvaluacionResponse.error("Respuesta vacía de la API.");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        System.out.println("Respuesta de evaluación de OpenAI:");
        System.out.println(content);

        // Parsear JSON a EvaluacionResponse
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> evaluacionJson = mapper.readValue(content, Map.class);

            EvaluacionResponse evaluacionResponse = new EvaluacionResponse();
            evaluacionResponse.setSuccess(true);
            evaluacionResponse.setPregunta(request.getQuestion());
            evaluacionResponse.setRespuesta(request.getAnswer());
            evaluacionResponse.setClaridadEstructura((Integer) evaluacionJson.get("claridad_estructura"));
            evaluacionResponse.setDominioTecnico((Integer) evaluacionJson.get("dominio_tecnico"));
            evaluacionResponse.setPertinencia((Integer) evaluacionJson.get("pertinencia"));
            evaluacionResponse.setComunicacionSeguridad((Integer) evaluacionJson.get("comunicacion_seguridad"));
            evaluacionResponse.setFortalezas((List<String>) evaluacionJson.get("fortalezas"));
            evaluacionResponse.setOportunidadesMejora((List<String>) evaluacionJson.get("oportunidades_mejora"));
            evaluacionResponse.setPuntuacionFinal(request.getValorPregunta());

            // GUARDAR EN LA BASE DE DATOS SOLO LOS CAMPOS NECESARIOS
            Evaluacion evaluacion = new Evaluacion();
            evaluacion.setFechaEvaluacion(new Date());
            evaluacion.setEvaluacionCompleta(content);
            evaluacion.setEstado("completada");

            if (request.getIdPostulacion() != null) {
                evaluacion.setPostulacion(postulacionRepository.findById(request.getIdPostulacion()).orElse(null));
            }

            evaluacionRepository.save(evaluacion);

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
            Valor de la pregunta: %d puntos
            
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
            
            Responde ÚNICAMENTE con este formato JSON:
            {
              "claridad_estructura": 0,
              "dominio_tecnico": 0,
              "pertinencia": 0,
              "comunicacion_seguridad": 0,
              "fortalezas": ["fortaleza específica 1", "fortaleza específica 2"],
              "oportunidades_mejora": ["mejora concreta 1", "mejora concreta 2", "mejora concreta 3"]
            }
            """.formatted(puesto, pregunta, respuesta, valorPregunta);
    }

    private Map<String, Object> enviarAOpenAIConTokens(String prompt) {
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3 // Temperatura más baja para ser más consistente y estricto
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorReturn(Collections.emptyMap())
                .block();
    }
}

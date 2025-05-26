package com.example.entrevista.service;

import com.example.entrevista.DTO.PreguntaRequest;
import com.example.entrevista.DTO.PreguntaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PreguntaService {

    private final WebClient webClient;

    public PreguntaService(WebClient openAIWebClient) {
        this.webClient = openAIWebClient;
    }

    public PreguntaResponse generarPreguntas(PreguntaRequest request) {
        String prompt = """
            Eres un generador automático de preguntas de entrevista.
            Tu tarea es generar 10 preguntas en formato JSON que evalúen a un candidato para el puesto de '%s'.

            Las preguntas deben cubrir los siguientes tipos:
            - technical_knowledge (2)
            - experience (2)
            - problem_solving (1)
            - tools (1)
            - methodology (1)
            - teamwork (1)
            - challenge (1)
            - best_practices (1)

            Instrucciones:
            - Las preguntas deben ser concretas, profundas y desafiantes.
            - Evita preguntas triviales o definiciones simples.
            - Cada pregunta debe comenzar con '¿' y terminar en '?'.
            - Cada pregunta debe tener al menos 15 palabras y fomentar respuestas reflexivas y elaboradas.
            - Asigna un valor "score" a cada pregunta que indique su peso en porcentaje (entero) para un total de 100 entre todas las preguntas.
            - Las preguntas más complejas y de tipos técnicos deben tener mayor valor.

            Responde únicamente con un JSON en este formato:
            [
              { "type": "tipo_de_pregunta", "question": "Texto de la pregunta", "score": valor_en_porcentaje },
              ...
            ]
            """.formatted(request.getPuesto());

        String jsonResponse = enviarAOpenAI(prompt);

        List<PreguntaResponse.PreguntaDTO> preguntas;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            PreguntaResponse.PreguntaDTO[] arr = mapper.readValue(jsonResponse, PreguntaResponse.PreguntaDTO[].class);
            preguntas = Arrays.asList(arr);
        } catch (Exception e) {
            System.out.println("Error al parsear JSON de preguntas: " + e.getMessage());
            PreguntaResponse.PreguntaDTO errorDto = new PreguntaResponse.PreguntaDTO();
            errorDto.setType("error");
            errorDto.setQuestion("No se pudo parsear la respuesta JSON de la API.");
            preguntas = List.of(errorDto);
        }

        PreguntaResponse response = new PreguntaResponse();
        response.setSuccess(true);
        response.setQuestions(preguntas);
        return response;
    }

    private String enviarAOpenAI(String prompt) {
        Map<String, Object> body = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7
        );

        Map<String, Object> response = webClient.post()
            .uri("/chat/completions")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .onErrorReturn(Collections.emptyMap())
            .block();

        if (response == null || response.isEmpty()) {
            System.out.println("No se pudo generar la pregunta.");
            return "[]";
        }

        System.out.println("Respuesta completa de OpenAI:");
        System.out.println(response);

        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            Integer promptTokens = (Integer) usage.get("prompt_tokens");
            Integer completionTokens = (Integer) usage.get("completion_tokens");
            Integer totalTokens = (Integer) usage.get("total_tokens");

            System.out.println("Tokens usados:");
            System.out.println(" - Prompt: " + promptTokens);
            System.out.println(" - Respuesta: " + completionTokens);
            System.out.println(" - Total: " + totalTokens);

            // Calculo costo aproximado con $0.002 por 1000 tokens para gpt-3.5-turbo
            double costPerThousandTokens = 0.002;
            double cost = (totalTokens / 1000.0) * costPerThousandTokens;
            System.out.printf("Costo aproximado: $%.6f USD%n", cost);
        } else {
            System.out.println("Información de tokens no disponible en la respuesta.");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } else {
            return "[]";
        }
    }
}

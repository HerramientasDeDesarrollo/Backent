package com.example.entrevista.service;

import com.example.entrevista.DTO.PreguntaRequest;
import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.repository.EntrevistaRepository;
import com.example.entrevista.model.Entrevista;
import com.example.entrevista.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PreguntaService {

    private final WebClient webClient;

    public PreguntaService(WebClient openAIWebClient) {
        this.webClient = openAIWebClient;
    }

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private EntrevistaRepository entrevistaRepository;

    public PreguntaResponse generarPreguntas(PreguntaRequest request) {
        String nivelDificultad = obtenerDescripcionDificultad(request.getDificultad());
        
        String prompt = """
            Eres un generador automático de preguntas de entrevista EXPERTO en recursos humanos y evaluación técnica.
            Tu tarea es generar 10 preguntas en formato JSON que evalúen a un candidato para el puesto de '%s'.
            
            NIVEL DE DIFICULTAD: %d/10 - %s

            Las preguntas deben cubrir los siguientes tipos:
            - technical_knowledge (2)
            - experience (2)
            - problem_solving (1)
            - tools (1)
            - methodology (1)
            - teamwork (1)
            - challenge (1)
            - best_practices (1)

            Instrucciones según el nivel de dificultad:
            %s

            Instrucciones generales:
            - Las preguntas deben ser concretas y desafiantes según el nivel especificado.
            - Cada pregunta debe comenzar con '¿' y terminar en '?'.
            - Cada pregunta debe tener al menos 15 palabras y fomentar respuestas reflexivas y elaboradas.
            - Asigna un valor "score" a cada pregunta que indique su peso en porcentaje (entero) para un total de 100 entre todas las preguntas.
            - Las preguntas más complejas y de tipos técnicos deben tener mayor valor.

            Responde únicamente con un JSON en este formato:
            [
              { "type": "tipo_de_pregunta", "question": "Texto de la pregunta", "score": valor_en_porcentaje },
              ...
            ]
            """.formatted(request.getPuesto(), request.getDificultad(), nivelDificultad, 
                         obtenerInstruccionesDificultad(request.getDificultad()));

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

        // Busca la entrevista
        Entrevista entrevista = entrevistaRepository.findById(request.getIdEntrevista())
            .orElseThrow(() -> new RuntimeException("Entrevista no encontrada"));

        // Guarda cada pregunta generada
        int numero = 1;
        for (PreguntaResponse.PreguntaDTO dto : preguntas) {
            Pregunta pregunta = new Pregunta();
            pregunta.setNumero(numero++);
            pregunta.setTextoPregunta(dto.getQuestion());
            pregunta.setDificultad(request.getDificultad()); // Guardar el nivel de dificultad
            pregunta.setEntrevista(entrevista);
            preguntaRepository.save(pregunta);
        }

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
            System.out.println("Costo a soles: S/ " + (cost * 3.64)); // Asumiendo 1 USD = 3.8 PEN
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

    private String obtenerDescripcionDificultad(int dificultad) {
        return switch (dificultad) {
            case 1, 2 -> "BÁSICO - Preguntas introductorias para candidatos junior";
            case 3, 4 -> "PRINCIPIANTE - Preguntas para candidatos con poca experiencia";
            case 5, 6 -> "INTERMEDIO - Preguntas para candidatos con experiencia moderada";
            case 7, 8 -> "AVANZADO - Preguntas desafiantes para candidatos senior";
            case 9, 10 -> "EXPERTO - Preguntas extremadamente complejas para especialistas";
            default -> "INTERMEDIO - Nivel estándar";
        };
    }

    private String obtenerInstruccionesDificultad(int dificultad) {
        return switch (dificultad) {
            case 1, 2 -> """
                - Enfócate en conceptos fundamentales y definiciones básicas
                - Pregunta sobre tareas cotidianas y responsabilidades básicas del puesto
                - Incluye preguntas sobre motivación y expectativas profesionales
                - Evita terminología muy técnica o escenarios complejos
                - Las preguntas deben ser accesibles para alguien con educación básica en el área
                """;
            case 3, 4 -> """
                - Incluye preguntas sobre experiencias prácticas simples
                - Pregunta sobre herramientas básicas del puesto
                - Incluye situaciones de trabajo en equipo sencillas
                - Pide ejemplos concretos pero no demasiado elaborados
                - Combina teoría básica con aplicación práctica
                """;
            case 5, 6 -> """
                - Pregunta sobre metodologías y mejores prácticas estándar
                - Incluye escenarios de resolución de problemas de complejidad media
                - Pide ejemplos específicos de proyectos o logros
                - Pregunta sobre liderazgo de equipos pequeños o tareas
                - Incluye preguntas sobre optimización y mejora de procesos
                """;
            case 7, 8 -> """
                - Enfócate en arquitectura, diseño de sistemas y decisiones técnicas complejas
                - Incluye escenarios de crisis o problemas críticos a resolver
                - Pregunta sobre liderazgo técnico y mentoría
                - Incluye preguntas sobre escalabilidad, rendimiento y optimización avanzada
                - Pide análisis profundo de trade-offs y decisiones estratégicas
                """;
            case 9, 10 -> """
                - Diseña preguntas que requieran conocimiento especializado profundo
                - Incluye escenarios hipotéticos extremadamente complejos
                - Pregunta sobre innovación, investigación y desarrollo de nuevas soluciones
                - Incluye preguntas sobre arquitectura empresarial y decisiones de alto nivel
                - Pide análisis de casos de estudio complejos con múltiples variables
                - Las preguntas deben desafiar incluso a expertos con años de experiencia
                """;
            default -> """
                - Equilibra preguntas teóricas y prácticas
                - Incluye escenarios de complejidad moderada
                - Pide ejemplos específicos y justificaciones
                """;
        };
    }
}
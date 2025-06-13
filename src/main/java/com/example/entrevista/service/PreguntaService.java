package com.example.entrevista.service;

import com.example.entrevista.DTO.PreguntaRequest;
import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.repository.ConvocatoriaRepository;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.model.Postulacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PreguntaService {

    private final WebClient webClient;
    
    @Value("${openai.api.key}")
    private String openaiApiKey;

    public PreguntaService(WebClient openAIWebClient) {
        this.webClient = openAIWebClient;
    }

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;

    public PreguntaResponse generarPreguntas(PreguntaRequest request) {
        // Obtener la postulación
        Postulacion postulacion = postulacionRepository.findById(request.getIdPostulacion())
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + request.getIdPostulacion()));
        
        // Obtener la convocatoria y el puesto
        Convocatoria convocatoria = postulacion.getConvocatoria();
        if (convocatoria == null) {
            throw new RuntimeException("La postulación no tiene una convocatoria asociada");
        }
        
        // Establecer los valores que faltan en el request
        request.setPuesto(convocatoria.getPuesto());
        request.setIdConvocatoria(convocatoria.getId());
        
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
            RECUERDA: La respuesta completa debe estar en ESPAÑOL. No mezcles idiomas.
            """.formatted(request.getPuesto(), request.getDificultad(), nivelDificultad,
                         obtenerInstruccionesDificultad(request.getDificultad()));

        String jsonResponse = enviarAOpenAI(prompt);

        List<PreguntaResponse.PreguntaDTO> preguntas;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            PreguntaResponse.PreguntaDTO[] arr = mapper.readValue(jsonResponse, PreguntaResponse.PreguntaDTO[].class);
            preguntas = Arrays.asList(arr);
        } catch (Exception e) {
            PreguntaResponse.PreguntaDTO errorDto = new PreguntaResponse.PreguntaDTO();
            errorDto.setType("error");
            errorDto.setQuestion("No se pudo parsear la respuesta JSON de la API: " + e.getMessage());
            preguntas = List.of(errorDto);
        }

        PreguntaResponse response = new PreguntaResponse();
        response.setSuccess(true);
        response.setQuestions(preguntas);

        // Guarda cada pregunta generada
        int numero = 1;
        for (PreguntaResponse.PreguntaDTO dto : preguntas) {
            Pregunta pregunta = new Pregunta();
            pregunta.setNumero(numero++);
            pregunta.setTextoPregunta(dto.getQuestion());
            pregunta.setDificultad(request.getDificultad());
            pregunta.setScore(dto.getScore()); // Save the score value from the DTO
            pregunta.setConvocatoria(convocatoria);
            pregunta.setPostulacion(postulacion); // Asociamos también a la postulación
            preguntaRepository.save(pregunta);
        }

        return response;
    }

    // Métodos para obtener preguntas
    public List<Pregunta> obtenerPreguntasPorConvocatoria(Long convocatoriaId) {
        return preguntaRepository.findByConvocatoriaId(convocatoriaId);
    }

    public List<Pregunta> obtenerPreguntasPorPostulacion(Long postulacionId) {
        return preguntaRepository.findByPostulacionId(postulacionId);
    }

    public List<Pregunta> obtenerPreguntasPorConvocatoriaYPostulacion(Long convocatoriaId, Long postulacionId) {
        return preguntaRepository.findByConvocatoriaIdAndPostulacionId(convocatoriaId, postulacionId);
    }

    private String enviarAOpenAI(String prompt) {
        Map<String, Object> body = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7
        );

        try {
            Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.isEmpty()) {
                System.err.println("OpenAI API returned empty response");
                return "[]";
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                
                // Ensure the content is properly formatted as JSON array
                if (content != null && !content.trim().startsWith("[")) {
                    // Extract JSON array if wrapped in backticks or other formatting
                    if (content.contains("[") && content.contains("]")) {
                        content = content.substring(content.indexOf("["), content.lastIndexOf("]") + 1);
                    }
                }
                
                return content;
            } else {
                System.err.println("No choices found in OpenAI response");
                return "[]";
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            e.printStackTrace();
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

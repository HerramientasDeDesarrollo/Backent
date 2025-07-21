package com.example.entrevista.service;

import com.example.entrevista.DTO.PreguntaRequest;
import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.repository.ConvocatoriaRepository;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.EntrevistaSessionRepository;
import com.example.entrevista.repository.EvaluacionRepository;
import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.model.Pregunta;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.EntrevistaSession;
import com.example.entrevista.model.Evaluacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PreguntaService {
    
    private static final Logger logger = LoggerFactory.getLogger(PreguntaService.class);
    
    // Definir constantes para los colores ANSI
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";
    
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
    
    @Autowired
    private EntrevistaSessionRepository entrevistaSessionRepository;
    
    @Autowired
    private EvaluacionRepository evaluacionRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public PreguntaResponse generarPreguntas(PreguntaRequest request) {
        logger.info(ANSI_CYAN + "Iniciando proceso de generación de preguntas para postulación ID: {}" + ANSI_RESET, request.getIdPostulacion());
        
        // Obtener la postulación
        logger.debug("Buscando postulación con ID: {}", request.getIdPostulacion());
        Postulacion postulacion = postulacionRepository.findById(request.getIdPostulacion())
            .orElseThrow(() -> {
                logger.error(ANSI_RED + "Postulación no encontrada con ID: {}" + ANSI_RESET, request.getIdPostulacion());
                return new RuntimeException("Postulación no encontrada con ID: " + request.getIdPostulacion());
            });
        
        // Verificar si las preguntas ya fueron generadas para esta postulación
        if (postulacion.isPreguntasGeneradas()) {
            logger.info(ANSI_YELLOW + "Las preguntas ya fueron generadas anteriormente para la postulación {}" + ANSI_RESET, postulacion.getId());
            List<Pregunta> preguntasExistentes = preguntaRepository.findByPostulacionId(postulacion.getId());
            
            if (!preguntasExistentes.isEmpty()) {
                logger.info(ANSI_BLUE + "Retornando {} preguntas existentes para la postulación" + ANSI_RESET, preguntasExistentes.size());
                
                // Convertir las preguntas existentes a DTOs para la respuesta
                List<PreguntaResponse.PreguntaDTO> preguntasDTO = preguntasExistentes.stream()
                    .map(pregunta -> {
                        PreguntaResponse.PreguntaDTO dto = new PreguntaResponse.PreguntaDTO();
                        dto.setQuestion(pregunta.getTextoPregunta());
                        dto.setScore(pregunta.getScore());
                        // Usamos el tipo guardado si existe, de lo contrario un valor genérico
                        dto.setType(pregunta.getTipo() != null ? pregunta.getTipo() : "existing_question");
                        dto.setTypeReadable(pregunta.getTipoLegible() != null ? pregunta.getTipoLegible() : traducirTipoPregunta(dto.getType()));
                        return dto;
                    })
                    .collect(Collectors.toList());
                
                PreguntaResponse response = new PreguntaResponse();
                response.setSuccess(true);
                response.setQuestions(preguntasDTO);
                response.setMensaje("Las preguntas ya fueron generadas anteriormente y se están recuperando de la base de datos."); // Añadir mensaje
                return response;
            } else {
                // Este es un caso extraño donde la postulación está marcada como con preguntas generadas pero no hay preguntas
                logger.warn("La postulación está marcada como con preguntas generadas pero no se encontraron preguntas. Se generarán nuevas preguntas.");
                // Continuamos con el flujo normal para generar preguntas nuevas
            }
        } else {
            logger.info(ANSI_PURPLE + "No hay preguntas generadas para la postulación {}. Generando nuevas preguntas..." + ANSI_RESET, postulacion.getId());
        }
        
        // Obtener la convocatoria y el puesto
        Convocatoria convocatoria = postulacion.getConvocatoria();
        if (convocatoria == null) {
            logger.error("La postulación {} no tiene una convocatoria asociada", request.getIdPostulacion());
            throw new RuntimeException("La postulación no tiene una convocatoria asociada");
        }
        logger.debug("Convocatoria encontrada: {} ({})", convocatoria.getJobTitle(), convocatoria.getId());
        
        // Obtener la dificultad directamente de la convocatoria
        int dificultad = convocatoria.getDificultad();
        logger.info("Usando dificultad {} de la convocatoria", dificultad);
        
        // Establecer los valores que faltan en el request
        request.setPuesto(convocatoria.getJobTitle());
        request.setIdConvocatoria(convocatoria.getId());
        
        String nivelDificultad = obtenerDescripcionDificultad(dificultad);
        logger.info("Generando preguntas para puesto '{}' con nivel de dificultad {}/10 ({})", 
                request.getPuesto(), dificultad, nivelDificultad);
        
        // Construir el prompt para OpenAI
        logger.debug("Construyendo prompt para OpenAI");
        String prompt = """
            Eres un generador automático de preguntas de entrevista EXPERTO en recursos humanos y evaluación técnica.
            Tu tarea es generar 10 preguntas en formato JSON que evalúen a un candidato para el puesto de '%s'.

            NIVEL DE DIFICULTAD: %d/10 - %s

            Las preguntas deben cubrir los siguientes tipos:
            - technical_knowledge (2): Conocimientos técnicos específicos del puesto
            - experience (2): Experiencia previa relevante
            - problem_solving (1): Capacidad de resolución de problemas
            - tools (1): Manejo de herramientas específicas
            - methodology (1): Metodologías del área
            - teamwork (1): Trabajo en equipo y colaboración
            - challenge (1): Desafíos enfrentados
            - best_practices (1): Mejores prácticas del sector

            IMPORTANTE SOBRE LA DISTRIBUCIÓN DE PUNTAJES:
            - Asigna un valor "score" a cada pregunta (entero) que refleje su peso para la evaluación
            - Los scores de TODAS las preguntas deben SUMAR EXACTAMENTE 100 puntos
            - Distribuye los puntos según la complejidad real de cada pregunta
            - Las preguntas más complejas y de tipos técnicos deben tener mayor valor
            - Considera estos pesos aproximados por tipo:
              * Conocimientos técnicos: ~13-15 puntos/pregunta
              * Resolución de problemas: ~12-14 puntos
              * Experiencia: ~10-12 puntos
              * Metodologías: ~9-11 puntos
              * Mejores prácticas: ~9-11 puntos
              * Desafíos: ~8-10 puntos
              * Herramientas: ~7-9 puntos
              * Trabajo en equipo: ~7-9 puntos
            - Verifica que la suma total sea EXACTAMENTE 100 puntos antes de finalizar

            Instrucciones según el nivel de dificultad:
            %s

            Instrucciones generales:
            - Las preguntas deben ser concretas y desafiantes según el nivel especificado.
            - Cada pregunta debe comenzar con '¿' y terminar en '?'.
            - Cada pregunta debe tener al menos 15 palabras y fomentar respuestas reflexivas y elaboradas.

            Responde únicamente con un JSON en este formato:
            [
              { 
                "type": "technical_knowledge", 
                "typeReadable": "Conocimiento Técnico",
                "question": "¿Texto de la pregunta?", 
                "score": 15
              },
              ...
            ]
            
            IMPORTANTE: La suma total de todos los scores debe ser EXACTAMENTE 100.
            RECUERDA: La respuesta completa debe estar en ESPAÑOL. No mezcles idiomas.
            VERIFICA que el JSON sea válido antes de enviarlo.
            """.formatted(request.getPuesto(), dificultad, nivelDificultad,
                         obtenerInstruccionesDificultad(dificultad));

        logger.info("Enviando petición a OpenAI para generar preguntas...");
        String jsonResponse = enviarAOpenAI(prompt);

        List<PreguntaResponse.PreguntaDTO> preguntas;
        try {
            logger.debug("Parseando respuesta JSON de OpenAI");
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            PreguntaResponse.PreguntaDTO[] arr = mapper.readValue(jsonResponse, PreguntaResponse.PreguntaDTO[].class);
            preguntas = Arrays.asList(arr);
            
            // Añadir typeReadable a cada pregunta
            preguntas.forEach(pregunta -> {
                pregunta.setTypeReadable(traducirTipoPregunta(pregunta.getType()));
            });
            
            logger.info("Se han generado {} preguntas correctamente", preguntas.size());
        } catch (Exception e) {
            logger.error("Error al parsear la respuesta JSON: {}", e.getMessage(), e);
            PreguntaResponse.PreguntaDTO errorDto = new PreguntaResponse.PreguntaDTO();
            errorDto.setType("error");
            errorDto.setQuestion("No se pudo parsear la respuesta JSON de la API: " + e.getMessage());
            preguntas = List.of(errorDto);
        }

        PreguntaResponse response = new PreguntaResponse();
        response.setSuccess(true);
        response.setQuestions(preguntas);

        // Guarda cada pregunta generada
        logger.info("Guardando {} preguntas en la base de datos", preguntas.size());
        int numero = 1;
        for (PreguntaResponse.PreguntaDTO dto : preguntas) {
            Pregunta pregunta = new Pregunta();
            pregunta.setNumero(numero++);
            pregunta.setTextoPregunta(dto.getQuestion());
            pregunta.setTipo(dto.getType()); // Guardar el tipo de pregunta original
            pregunta.setTipoLegible(traducirTipoPregunta(dto.getType())); // Guardar la versión legible en español
            pregunta.setScore(dto.getScore());
            pregunta.setConvocatoria(convocatoria);
            pregunta.setPostulacion(postulacion);
            
            logger.debug("Guardando pregunta {}: {} (tipo: {}, tipo legible: {}, valor: {}%, dificultad: {})", 
                    numero-1, dto.getQuestion().substring(0, Math.min(50, dto.getQuestion().length())) + "...", 
                    dto.getType(), pregunta.getTipoLegible(), dto.getScore(), convocatoria.getDificultad());
            
            preguntaRepository.save(pregunta);
        }
        
        // Marcar la postulación como con preguntas generadas y guardarla
        postulacion.setPreguntasGeneradas(true);
        postulacionRepository.save(postulacion);
        logger.info(ANSI_GREEN + "✓ Postulación {} marcada como con preguntas generadas" + ANSI_RESET, postulacion.getId());

        logger.info(ANSI_GREEN + "✓ Proceso de generación de preguntas completado exitosamente" + ANSI_RESET);
        response.setMensaje("Las preguntas se han generado por primera vez para esta postulación."); // Añadir mensaje
        return response;
    }
    
    // Nuevo método que incluye tracking de respuestas
    public PreguntaResponse generarPreguntasConEstado(PreguntaRequest request) {
        logger.info(ANSI_CYAN + "Generando preguntas con estado de respuestas para postulación ID: {}" + ANSI_RESET, request.getIdPostulacion());
        
        // Primero generar/obtener las preguntas usando el método existente
        PreguntaResponse baseResponse = generarPreguntas(request);
        
        if (!baseResponse.isSuccess()) {
            return baseResponse; // Si hay error, retornar tal como está
        }
        
        // Obtener la postulación
        Postulacion postulacion = postulacionRepository.findById(request.getIdPostulacion())
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
        
        // Obtener preguntas de la base de datos con IDs
        List<Pregunta> preguntasDB = preguntaRepository.findByPostulacionId(request.getIdPostulacion());
        
        // Buscar EntrevistaSession si existe (para el ID de sesión)
        Optional<EntrevistaSession> sessionOpt = entrevistaSessionRepository.findByPostulacionId(request.getIdPostulacion());
        
        // Obtener evaluaciones de la postulación para verificar qué preguntas han sido respondidas
        List<Evaluacion> evaluaciones = evaluacionRepository.findByPostulacionId(request.getIdPostulacion());
        
        // Crear un mapa pregunta_id -> evaluacion para búsqueda rápida
        Map<Long, Evaluacion> evaluacionesPorPregunta = evaluaciones.stream()
            .filter(eval -> eval.getPregunta() != null)
            .collect(Collectors.toMap(
                eval -> eval.getPregunta().getId(),
                eval -> eval,
                (existing, replacement) -> existing // En caso de duplicados, mantener el primero
            ));
        
        // Enriquecer los DTOs con información de estado
        List<PreguntaResponse.PreguntaDTO> preguntasEnriquecidas = new ArrayList<>();
        Map<String, Boolean> estadoRespuestas = new HashMap<>();
        int preguntasRespondidas = 0;
        
        for (int i = 0; i < baseResponse.getQuestions().size() && i < preguntasDB.size(); i++) {
            PreguntaResponse.PreguntaDTO dto = baseResponse.getQuestions().get(i);
            Pregunta preguntaDB = preguntasDB.get(i);
            
            // Establecer ID de la pregunta
            dto.setId(preguntaDB.getId());
            
            // Verificar si está respondida usando la tabla Evaluacion
            Evaluacion evaluacion = evaluacionesPorPregunta.get(preguntaDB.getId());
            boolean respondida = evaluacion != null;
            boolean evaluada = false;
            String respuesta = null;
            String fechaRespuesta = null;
            
            if (evaluacion != null) {
                preguntasRespondidas++;
                respuesta = evaluacion.getRespuesta();
                fechaRespuesta = evaluacion.getFechaEvaluacion() != null ? 
                    evaluacion.getFechaEvaluacion().toString() : null;
                
                // Verificar si la evaluación está completa (tiene todos los campos evaluados)
                evaluada = evaluacion.getClaridadEstructura() != null && 
                          evaluacion.getDominioTecnico() != null && 
                          evaluacion.getPertinencia() != null && 
                          evaluacion.getComunicacionSeguridad() != null &&
                          evaluacion.getPorcentajeObtenido() != null;
            }
            
            // Establecer estado en el DTO
            dto.setRespondida(respondida);
            dto.setRespuesta(respuesta);
            dto.setFechaRespuesta(fechaRespuesta);
            dto.setEvaluada(evaluada);
            
            // Agregar al mapa de estado
            estadoRespuestas.put(preguntaDB.getId().toString(), respondida);
            
            preguntasEnriquecidas.add(dto);
        }
        
        // Calcular estadísticas
        int totalPreguntas = preguntasEnriquecidas.size();
        int preguntasPendientes = totalPreguntas - preguntasRespondidas;
        double progresoRespuestas = totalPreguntas > 0 ? (double) preguntasRespondidas / totalPreguntas * 100 : 0;
        
        // Actualizar la respuesta con información de estado
        baseResponse.setQuestions(preguntasEnriquecidas);
        baseResponse.setTotalPreguntas(totalPreguntas);
        baseResponse.setPreguntasRespondidas(preguntasRespondidas);
        baseResponse.setPreguntasPendientes(preguntasPendientes);
        baseResponse.setProgresoRespuestas(Math.round(progresoRespuestas * 100.0) / 100.0);
        baseResponse.setEstadoRespuestas(estadoRespuestas);
        
        // Agregar EntrevistaSession ID - priorizar el existente o crear uno nuevo
        Long sessionId = null;
        if (sessionOpt.isPresent()) {
            sessionId = sessionOpt.get().getId();
            logger.debug("Usando EntrevistaSession existente: {}", sessionId);
        } else if (postulacion.getEntrevistaSessionId() != null) {
            sessionId = postulacion.getEntrevistaSessionId();
            logger.debug("Usando sessionId de postulación: {}", sessionId);
        } else {
            // Si no existe session, intentar crear una nueva usando PostulacionService
            try {
                logger.warn("No se encontró sessionId para postulación {}. Se requiere crear sesión primero.", request.getIdPostulacion());
            } catch (Exception e) {
                logger.error("Error al intentar crear sesión para postulación {}: {}", request.getIdPostulacion(), e.getMessage());
            }
        }
        
        if (sessionId != null) {
            baseResponse.setEntrevistaSessionId(sessionId);
            logger.info("SessionId {} agregado a la respuesta de preguntas", sessionId);
        }
        
        // Actualizar mensaje
        String mensajeEstado = String.format("Preguntas cargadas: %d total, %d respondidas (%.1f%% progreso)", 
            totalPreguntas, preguntasRespondidas, progresoRespuestas);
        baseResponse.setMensaje(baseResponse.getMensaje() + " " + mensajeEstado);
        
        logger.info(ANSI_GREEN + "✓ Preguntas generadas con estado: {} total, {} respondidas" + ANSI_RESET, 
            totalPreguntas, preguntasRespondidas);
        
        return baseResponse;
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
            logger.debug("Enviando solicitud a la API de OpenAI");
            Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.isEmpty()) {
                logger.error("OpenAI API devolvió una respuesta vacía");
                return "[]";
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                logger.debug("Respuesta de OpenAI recibida correctamente");
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                
                // Ensure the content is properly formatted as JSON array
                if (content != null && !content.trim().startsWith("[")) {
                    // Extract JSON array if wrapped in backticks or other formatting
                    if (content.contains("[") && content.contains("]")) {
                        logger.debug("Formateando contenido JSON de la respuesta");
                        content = content.substring(content.indexOf("["), content.lastIndexOf("]") + 1);
                    }
                }
                
                return content;
            } else {
                logger.error("No se encontraron opciones en la respuesta de OpenAI");
                return "[]";
            }
        } catch (Exception e) {
            logger.error("Error al llamar a la API de OpenAI: {}", e.getMessage(), e);
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

    private String traducirTipoPregunta(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "technical_knowledge" -> "Conocimiento Técnico";
            case "experience" -> "Experiencia";
            case "problem_solving" -> "Resolución de Problemas";
            case "tools" -> "Herramientas";
            case "methodology" -> "Metodología";
            case "teamwork" -> "Trabajo en Equipo";
            case "challenge" -> "Desafío";
            case "best_practices" -> "Mejores Prácticas";
            default -> tipo; // Si no hay traducción, devolver el original
        };
    }
}

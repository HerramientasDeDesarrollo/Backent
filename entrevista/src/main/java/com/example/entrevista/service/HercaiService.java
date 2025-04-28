package com.example.entrevista.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.entrevista.DTO.Pregunta;
import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.repository.EntrevistaRepository;
import com.example.entrevista.model.Entrevista;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@Slf4j
public class HercaiService {
    private final RestTemplate restTemplate;
    private final PreguntaRepository preguntaRepository;
    private final EntrevistaRepository entrevistaRepository;

    @Value("${hercai.url}")
    private String hercaiUrl;

    public HercaiService(RestTemplate restTemplate, PreguntaRepository preguntaRepository, EntrevistaRepository entrevistaRepository) {
        this.restTemplate = restTemplate;
        this.preguntaRepository = preguntaRepository;
        this.entrevistaRepository = entrevistaRepository;
    }

    public PreguntaResponse obtenerPreguntas() {
        try {
            log.info("Intentando obtener preguntas de Hercai en: {}", hercaiUrl);
            ResponseEntity<PreguntaResponse> response = restTemplate.getForEntity(
                hercaiUrl + "/preguntas", 
                PreguntaResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Preguntas obtenidas exitosamente");

                // Guardar preguntas en la base de datos
                response.getBody().getQuestions().forEach(preguntaRepository::save);

                return response.getBody();
            } else {
                throw new RuntimeException("Respuesta inválida del servicio Hercai");
            }
        } catch (Exception e) {
            log.error("Error al obtener preguntas de Hercai: {}", e.getMessage());
            throw new RuntimeException("Error al comunicarse con Hercai", e);
        }
    }

    public Map<String, Object> evaluarRespuesta(String pregunta, String respuesta) {
        try {
            log.info("Evaluando respuesta para la pregunta: {}", pregunta);

            // Crear el cuerpo de la solicitud para el API externo
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("question", pregunta);
            requestBody.put("answer", respuesta);

            log.info("Enviando solicitud al API externo: {}", hercaiUrl + "/evaluar");
            log.info("Cuerpo de la solicitud: {}", requestBody);

            // Enviar la solicitud al endpoint /evaluar del API externo
            ResponseEntity<Map> response = restTemplate.postForEntity(
                hercaiUrl + "/evaluar",
                new HttpEntity<>(requestBody),
                Map.class
            );

            log.info("Respuesta del API externo: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Evaluación completada exitosamente: {}", response.getBody());

                // Extraer los datos de la evaluación
                Map<String, Object> evaluacion = response.getBody();
                Map<String, Object> evaluacionDetallada = (Map<String, Object>) evaluacion.get("evaluation");
                Map<String, Object> detalles = (Map<String, Object>) evaluacionDetallada.get("evaluacion_detallada");

                // Convertir listas a cadenas separadas por comas
                String fortalezas = String.join(", ", (List<String>) evaluacionDetallada.getOrDefault("fortalezas", List.of()));
                String oportunidadesMejora = String.join(", ", (List<String>) evaluacionDetallada.getOrDefault("oportunidades_mejora", List.of()));

                // Crear y guardar la entidad Entrevista
                Entrevista nuevaEntrevista = new Entrevista(
                    pregunta, // Pregunta como cadena
                    respuesta, // Respuesta como cadena
                    (String) detalles.getOrDefault("claridad_estructura", "No especificado"),
                    (String) detalles.getOrDefault("dominio_tecnico", "No especificado"),
                    (String) detalles.getOrDefault("pertinencia", "No especificado"),
                    (String) detalles.getOrDefault("comunicacion_seguridad", "No especificado"),
                    fortalezas, // Fortalezas como cadena
                    oportunidadesMejora, // Oportunidades de mejora como cadena
                    ((Number) evaluacionDetallada.getOrDefault("puntuacion_final", 0)).doubleValue()
                );

                log.info("Guardando la entidad Entrevista: {}", nuevaEntrevista);
                entrevistaRepository.save(nuevaEntrevista);

                return response.getBody();
            } else {
                throw new RuntimeException("Respuesta inválida del servicio Hercai al evaluar");
            }
        } catch (Exception e) {
            log.error("Error al evaluar respuesta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al comunicarse con Hercai para evaluar", e);
        }
    }
}
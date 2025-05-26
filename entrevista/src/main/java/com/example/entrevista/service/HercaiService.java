package com.example.entrevista.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;
import com.example.entrevista.repository.ResultadoRepository;
import com.example.entrevista.model.Resultado;
import com.example.entrevista.model.Pregunta;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
public class HercaiService {
    private final RestTemplate restTemplate;
    private final PreguntaRepository preguntaRepository;
    private final ResultadoRepository resultadoRepository;

    @Value("${hercai.url}")
    private String hercaiUrl;

    @Value("${hercai.api.url}")
    private String hercaiApiUrl;

    public HercaiService(RestTemplate restTemplate, PreguntaRepository preguntaRepository, ResultadoRepository resultadoRepository) {
        this.restTemplate = restTemplate;
        this.preguntaRepository = preguntaRepository;
        this.resultadoRepository = resultadoRepository;
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
                int numero = 1;
                for (String texto : response.getBody().getQuestions()) {
                    Pregunta pregunta = new Pregunta();
                    pregunta.setNumero(numero++);
                    pregunta.setTextoPregunta(texto);
                    preguntaRepository.save(pregunta);
                }

                return response.getBody();
            } else {
                throw new RuntimeException("Respuesta inválida del servicio Hercai");
            }
        } catch (Exception e) {
            log.error("Error al obtener preguntas de Hercai: {}", e.getMessage());
            throw new RuntimeException("Error al comunicarse con Hercai", e);
        }
    }

    public PreguntaResponse obtenerPreguntas(String puesto) {
        try {
            log.info("Intentando obtener preguntas de Hercai en: {}", hercaiUrl);

            Map<String, String> body = new HashMap<>();
            body.put("puesto", puesto);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body);

            ResponseEntity<PreguntaResponse> response = restTemplate.postForEntity(
                hercaiUrl + "/preguntas",
                request,
                PreguntaResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Preguntas obtenidas exitosamente");

                // Transforma los textos en entidades Pregunta y guárdalas
                List<String> preguntasTexto = response.getBody().getQuestions();
                int numero = 1;
                for (String texto : preguntasTexto) {
                    Pregunta pregunta = new Pregunta();
                    pregunta.setNumero(numero++);
                    pregunta.setTextoPregunta(texto);
                    preguntaRepository.save(pregunta);
                }

                return response.getBody();
            } else {
                throw new RuntimeException("Respuesta inválida del servicio Hercai");
            }
        } catch (Exception e) {
            log.error("Error al obtener preguntas de Hercai: {}", e.getMessage());
            throw new RuntimeException("Error al comunicarse con Hercai", e);
        }
    }
    
    public Map<String, Object> evaluarRespuesta(String pregunta, String respuesta, String puesto) {
        try {
            log.info("Evaluando respuesta para la pregunta: {}", pregunta);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("question", pregunta);
            requestBody.put("answer", respuesta);
            requestBody.put("puesto", puesto);

            log.info("Enviando solicitud al API externo: {}", hercaiUrl + "/evaluar");
            log.info("Cuerpo de la solicitud: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                hercaiUrl + "/evaluar",
                new HttpEntity<>(requestBody),
                Map.class
            );

            log.info("Respuesta del API externo: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Evaluación completada exitosamente: {}", response.getBody());

                Map<String, Object> evaluacion = response.getBody();
                Map<String, Object> evaluacionDetallada = (Map<String, Object>) evaluacion.get("evaluation");
                Map<String, Object> detalles = (Map<String, Object>) evaluacionDetallada.get("evaluacion_detallada");

                String fortalezas = String.join(", ", (List<String>) evaluacionDetallada.getOrDefault("fortalezas", List.of()));
                String oportunidadesMejora = String.join(", ", (List<String>) evaluacionDetallada.getOrDefault("oportunidades_mejora", List.of()));

                Resultado nuevoResultado = new Resultado(
                    pregunta,
                    respuesta,
                    (String) detalles.getOrDefault("claridad_estructura", "No especificado"),
                    (String) detalles.getOrDefault("dominio_tecnico", "No especificado"),
                    (String) detalles.getOrDefault("pertinencia", "No especificado"),
                    (String) detalles.getOrDefault("comunicacion_seguridad", "No especificado"),
                    fortalezas,
                    oportunidadesMejora,
                    ((Number) evaluacionDetallada.getOrDefault("puntuacion_final", 0)).doubleValue(),
                    puesto
                );

                log.info("Guardando la entidad Resultado: {}", nuevoResultado);
                resultadoRepository.save(nuevoResultado);

                return response.getBody();
            } else {
                throw new RuntimeException("Respuesta inválida del servicio Hercai al evaluar");
            }
        } catch (Exception e) {
            log.error("Error al evaluar respuesta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al comunicarse con Hercai para evaluar", e);
        }
    }

    public List<String> obtenerPreguntas(String puesto, int limit) {
        String url = hercaiApiUrl + "/preguntas";
        Map<String, Object> request = new HashMap<>();
        request.put("puesto", puesto);
        request.put("limit", limit);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody().get("success"))) {
            List<Map<String, Object>> questions = (List<Map<String, Object>>) response.getBody().get("questions");
            List<String> preguntas = new ArrayList<>();
            for (Map<String, Object> q : questions) {
                preguntas.add((String) q.get("question"));
            }
            return preguntas;
        } else {
            throw new RuntimeException("No se pudieron obtener preguntas de Hercai");
        }
    }

    public Map<String, Object> evaluarRespuesta(String pregunta, String respuesta) {
        String url = hercaiApiUrl + "/evaluar";
        Map<String, Object> request = new HashMap<>();
        request.put("question", pregunta);
        request.put("answer", respuesta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody().get("success"))) {
            return (Map<String, Object>) response.getBody().get("evaluation");
        } else {
            throw new RuntimeException("No se pudo evaluar la respuesta");
        }
    }
}
package com.example.entrevista.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.entrevista.DTO.Pregunta;
import com.example.entrevista.DTO.PreguntaResponse;
import com.example.entrevista.repository.PreguntaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HercaiService {
    private final RestTemplate restTemplate;
    private final PreguntaRepository preguntaRepository;

    @Value("${hercai.url}")
    private String hercaiUrl;

    public HercaiService(RestTemplate restTemplate, PreguntaRepository preguntaRepository) {
        this.restTemplate = restTemplate;
        this.preguntaRepository = preguntaRepository;
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
}
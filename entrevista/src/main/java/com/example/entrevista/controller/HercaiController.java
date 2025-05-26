package com.example.entrevista.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.entrevista.service.HercaiService;
import com.example.entrevista.DTO.PreguntaResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/entrevistas")
public class HercaiController {
    private final HercaiService hercaiService;

    public HercaiController(HercaiService hercaiService) {
        this.hercaiService = hercaiService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarEntrevista(@RequestBody Map<String, Object> request) {
        String puesto = (String) request.get("puesto");
        Integer limit = request.get("limit") != null ? (Integer) request.get("limit") : 10;
        if (puesto == null || puesto.isBlank()) {
            return ResponseEntity.badRequest().body("El puesto es obligatorio.");
        }
        try {
            // Ahora retorna una lista de preguntas
            var preguntas = hercaiService.obtenerPreguntas(puesto, limit);
            return ResponseEntity.ok(Map.of("preguntas", preguntas));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluarRespuesta(@RequestBody Map<String, Object> request) {
        try {
            String pregunta = (String) request.get("pregunta");
            String respuesta = (String) request.get("respuesta");
            if (pregunta == null || respuesta == null) {
                return ResponseEntity.badRequest().body("La pregunta y la respuesta son obligatorias.");
            }
            // Llama al servicio con los nombres correctos para la nueva API
            var resultado = hercaiService.evaluarRespuesta(pregunta, respuesta);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}
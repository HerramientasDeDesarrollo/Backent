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

    @GetMapping("/iniciar")
    public ResponseEntity<PreguntaResponse> iniciarEntrevista() {
        PreguntaResponse preguntas = hercaiService.obtenerPreguntas();
        return ResponseEntity.ok(preguntas);
    }

    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluarRespuesta(@RequestBody Map<String, String> request) {
        try {
            // Obtener pregunta y respuesta del cuerpo de la solicitud
            String pregunta = request.get("pregunta");
            String respuesta = request.get("respuesta");

            // Validar que ambos campos estén presentes
            if (pregunta == null || respuesta == null) {
                return ResponseEntity.badRequest().body("La pregunta y la respuesta son obligatorias.");
            }

            // Llamar al servicio para evaluar la respuesta
            Map<String, Object> resultado = hercaiService.evaluarRespuesta(pregunta, respuesta);

            // Devolver el resultado al cliente
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            // Manejar cualquier excepción y devolver un error 500 con el mensaje
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}
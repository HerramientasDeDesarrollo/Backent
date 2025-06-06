package com.example.entrevista.controller;

import com.example.entrevista.DTO.*;
import com.example.entrevista.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    // Solo EMPRESA puede generar preguntas con IA
    @PreAuthorize("hasRole('EMPRESA')")
    @PostMapping("/generar")
    public ResponseEntity<?> generarPreguntas(@RequestBody PreguntaRequest request) {
        try {
            // Validar que la dificultad esté en el rango correcto
            if (request.getDificultad() < 1 || request.getDificultad() > 10) {
                return ResponseEntity.badRequest()
                    .body("La dificultad debe estar entre 1 y 10");
            }
            
            PreguntaResponse response = preguntaService.generarPreguntas(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al generar preguntas: " + e.getMessage());
        }
    }

}
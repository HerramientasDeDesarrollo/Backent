package com.example.entrevista.controller;

import com.example.entrevista.DTO.*;
import com.example.entrevista.service.PreguntaService;
import com.example.entrevista.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    // USUARIO puede generar preguntas con IA
    @PreAuthorize("hasRole('USUARIO')")
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

    // USUARIO puede ver solo sus propias preguntas por postulación
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-preguntas/postulacion/{postulacionId}")
    public ResponseEntity<?> verMisPreguntasPorPostulacion(@PathVariable Long postulacionId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorPostulacion(postulacionId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    // USUARIO puede ver preguntas por entrevista
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-preguntas/{entrevistaId}")
    public ResponseEntity<?> verMisPreguntas(@PathVariable Long entrevistaId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorEntrevista(entrevistaId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    // EMPRESA puede ver preguntas de sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/por-entrevista/{entrevistaId}")
    public ResponseEntity<?> verPreguntasPorEntrevista(@PathVariable Long entrevistaId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorEntrevista(entrevistaId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    // Nuevo endpoint para obtener preguntas por entrevista y postulación
    @PreAuthorize("hasAnyRole('USUARIO', 'EMPRESA', 'ADMIN')")
    @GetMapping("/entrevista/{entrevistaId}/postulacion/{postulacionId}")
    public ResponseEntity<?> verPreguntasPorEntrevistaYPostulacion(
            @PathVariable Long entrevistaId, 
            @PathVariable Long postulacionId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorEntrevistaYPostulacion(entrevistaId, postulacionId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }
}
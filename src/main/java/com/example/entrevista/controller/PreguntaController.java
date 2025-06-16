package com.example.entrevista.controller;

import com.example.entrevista.DTO.*;
import com.example.entrevista.service.PreguntaService;
import com.example.entrevista.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @PostMapping("/generar")
    public ResponseEntity<?> generarPreguntas(@RequestBody PreguntaRequest request) {
        try {
            // Validación de campos obligatorios
            if (request.getIdPostulacion() == null) {
                return ResponseEntity.badRequest().body("El ID de postulación es obligatorio");
            }
            
            PreguntaResponse response = preguntaService.generarPreguntas(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al generar preguntas: " + e.getMessage());
        }
    }

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

    @GetMapping("/mis-preguntas/convocatoria/{convocatoriaId}")
    public ResponseEntity<?> verMisPreguntasPorConvocatoria(@PathVariable Long convocatoriaId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorConvocatoria(convocatoriaId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    @GetMapping("/convocatoria/{convocatoriaId}/postulacion/{postulacionId}")
    public ResponseEntity<?> verPreguntasPorConvocatoriaYPostulacion(
            @PathVariable Long convocatoriaId,
            @PathVariable Long postulacionId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorConvocatoriaYPostulacion(convocatoriaId, postulacionId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }
}
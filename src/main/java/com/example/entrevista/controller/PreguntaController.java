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
    private PreguntaService preguntaService;    // Solo usuarios pueden generar preguntas para sus entrevistas
    @PostMapping("/generar")
    @PreAuthorize("hasRole('USUARIO')")
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

    // Solo usuarios pueden ver sus preguntas por postulación
    @GetMapping("/postulacion/{postulacionId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> verPreguntasPorPostulacion(@PathVariable Long postulacionId) {
        try {
            List<Pregunta> preguntas = preguntaService.obtenerPreguntasPorPostulacion(postulacionId);
            return ResponseEntity.ok(preguntas);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    // Empresas pueden ver preguntas de sus convocatorias para análisis
    @GetMapping("/convocatoria/{convocatoriaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> verPreguntasPorConvocatoria(@PathVariable Long convocatoriaId) {
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

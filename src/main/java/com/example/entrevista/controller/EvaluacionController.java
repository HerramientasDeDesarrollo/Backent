package com.example.entrevista.controller;

import com.example.entrevista.DTO.EvaluacionRequest;
import com.example.entrevista.DTO.EvaluacionResponse;
import com.example.entrevista.service.EvaluacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    @Autowired
    private EvaluacionService evaluacionService;


    // USUARIO puede evaluar una respuesta simple
    @PreAuthorize("hasRole('USUARIO')")
    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluarRespuesta(@RequestBody EvaluacionRequest request) {
        try {
            EvaluacionResponse evaluacion = evaluacionService.evaluarPregunta(request);
            
            if (evaluacion.isSuccess()) {
                // Opcionalmente guardar el resultado en la base de datos
                // guardarResultado(request, evaluacion);
            }
            
            return ResponseEntity.ok(evaluacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                EvaluacionResponse.error("Error al evaluar: " + e.getMessage())
            );
        }
    }

    // USUARIO puede ver solo sus propios resultados de evaluación
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-resultados/{postulacionId}")
    public ResponseEntity<?> verMisResultados(@PathVariable Long postulacionId) {
        // Aquí podrías implementar la lógica para obtener resultados por postulación
        return ResponseEntity.ok("Resultados para postulación: " + postulacionId);
    }

    // EMPRESA puede ver resultados de evaluaciones de sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/por-entrevista/{entrevistaId}")
    public ResponseEntity<?> verResultadosPorEntrevista(@PathVariable Long entrevistaId) {
        // Aquí podrías implementar la lógica para obtener resultados por entrevista
        return ResponseEntity.ok("Resultados para entrevista: " + entrevistaId);
    }
}

package com.example.entrevista.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.entrevista.service.HercaiService;
import com.example.entrevista.DTO.PreguntaResponse;

@RestController
@RequestMapping("/api/entrevistas")
public class EntrevistaController {
    private final HercaiService hercaiService;

    public EntrevistaController(HercaiService hercaiService) {
        this.hercaiService = hercaiService;
    }

    @GetMapping("/iniciar")
    public ResponseEntity<PreguntaResponse> iniciarEntrevista() {
        PreguntaResponse preguntas = hercaiService.obtenerPreguntas();
        return ResponseEntity.ok(preguntas);
    }
}
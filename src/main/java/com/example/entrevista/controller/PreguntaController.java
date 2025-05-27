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
        // lógica para generar preguntas usando IA
        return null;
    }

}
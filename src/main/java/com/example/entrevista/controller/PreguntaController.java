package com.example.entrevista.controller;

import com.example.entrevista.DTO.*;
import com.example.entrevista.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @PostMapping
    public PreguntaResponse generarPreguntas(@RequestBody PreguntaRequest request) {
        return preguntaService.generarPreguntas(request);
    }
}
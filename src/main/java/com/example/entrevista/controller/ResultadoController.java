package com.example.entrevista.controller;


import com.example.entrevista.service.ResultadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resultados")
public class ResultadoController {

    @Autowired
    private ResultadoService resultadoService;

    // USUARIO puede ver solo sus propios resultados
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-resultados")
    public ResponseEntity<?> verMisResultados() {
        return null;
    }

    // EMPRESA puede ver resultados de sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/por-entrevista/{idEntrevista}")
    public ResponseEntity<?> verResultadosPorEntrevista(@PathVariable Long idEntrevista) {
        return null;
    }

    // ADMIN puede ver todos los resultados (opcional)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> verTodosLosResultados() {
        return null;
    }
}

package com.example.entrevista.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/respuestas")
public class RespuestaController {

    @Autowired

    // USUARIO puede ver solo sus propias respuestas
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-respuestas")
    public ResponseEntity<?> verMisRespuestas() {
        // lógica: obtener usuario autenticado y filtrar respuestas
        return null;
    }

    // EMPRESA puede ver respuestas de sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/por-entrevista/{idEntrevista}")
    public ResponseEntity<?> verRespuestasPorEntrevista(@PathVariable Long idEntrevista) {
        // lógica: validar que la entrevista pertenezca a la empresa autenticada y mostrar respuestas
        return null;
    }

    // ADMIN puede ver todas las respuestas (opcional)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> verTodasLasRespuestas() {
        return null;
    }

    // No hay endpoints para modificar respuestas
}

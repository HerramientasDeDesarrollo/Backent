package com.example.entrevista.controller;

import com.example.entrevista.model.Postulacion;
import com.example.entrevista.service.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;

    // Solo USUARIO puede crear una postulación
    @PreAuthorize("hasRole('USUARIO')")
    @PostMapping
    public ResponseEntity<?> crearPostulacion(@RequestBody Postulacion postulacion) {
        return null;
    }

    // USUARIO solo puede ver sus propias postulaciones
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-postulaciones")
    public ResponseEntity<?> verMisPostulaciones() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        return null;
    }

    // USUARIO puede buscar sus postulaciones por empresa
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/mis-postulaciones/empresa/{empresaId}")
    public ResponseEntity<?> buscarPostulacionesPorEmpresa(@PathVariable Long empresaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoUsuario = auth.getName();
        return null;
    }

    // EMPRESA solo puede ver postulantes de sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/por-entrevista/{idEntrevista}")
    public ResponseEntity<?> verPostulantes(@PathVariable Long idEntrevista) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoEmpresa = auth.getName();
        return null;
    }

    // ADMIN puede ver todas las postulaciones
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> verTodasLasPostulaciones() {
        return null;
    }
}

package com.example.entrevista.controller;

import com.example.entrevista.model.Entrevista;
import com.example.entrevista.service.EntrevistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entrevistas")
public class EntrevistaController {

    @Autowired
    private EntrevistaService entrevistaService;

    // ADMIN puede ver todas las entrevistas
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> verTodasLasEntrevistas() {
        return ResponseEntity.ok(entrevistaService.findAll());
    }

    // EMPRESA puede crear entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @PostMapping
    public ResponseEntity<?> crearEntrevista(@RequestBody Entrevista entrevista) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoEmpresa = auth.getName();
        return ResponseEntity.ok(entrevistaService.save(entrevista));
    }

    // EMPRESA puede ver solo sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/mis-entrevistas")
    public ResponseEntity<?> verMisEntrevistas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoEmpresa = auth.getName();
        return null;
    }

    // EMPRESA puede activar/desactivar o borrar solo sus entrevistas
    @PreAuthorize("hasRole('EMPRESA')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoEntrevista(@PathVariable Long id, @RequestParam boolean activa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoEmpresa = auth.getName();
        return null;
    }

    //EMPRESA puede borrar entrevistas asociadas a su cuenta
    @PreAuthorize("hasRole('EMPRESA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarEntrevista(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoEmpresa = auth.getName();
        return null;
    }

    // USUARIO solo puede ver el estado de entrevistas a las que se postuló
    @PreAuthorize("hasRole('USUARIO')")
    @GetMapping("/estado/{idEntrevista}")
    public ResponseEntity<?> verEstadoEntrevista(@PathVariable Long idEntrevista) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correoUsuario = auth.getName();
        // lógica para mostrar estado solo si el usuario está postulado a esa entrevista
        return null;
    }
}

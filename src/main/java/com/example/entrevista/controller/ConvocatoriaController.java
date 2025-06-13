package com.example.entrevista.controller;

import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.service.ConvocatoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @PostMapping
    public ResponseEntity<Convocatoria> crear(@RequestBody Convocatoria convocatoria) {
        return ResponseEntity.ok(convocatoriaService.crearConvocatoria(convocatoria));
    }

    @GetMapping
    public ResponseEntity<List<Convocatoria>> listarTodas() {
        return ResponseEntity.ok(convocatoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Convocatoria> buscarPorId(@PathVariable Long id) {
        return convocatoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Getting convocatorias by empresa ID
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Convocatoria>> buscarPorEmpresa(@PathVariable Long empresaId) {
        List<Convocatoria> convocatorias = convocatoriaService.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(convocatorias);
    }

    // Getting active convocatorias
    @GetMapping("/activas")
    public ResponseEntity<List<Convocatoria>> listarActivas() {
        List<Convocatoria> convocatorias = convocatoriaService.listarActivas();
        return ResponseEntity.ok(convocatorias);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        convocatoriaService.eliminarConvocatoria(id);
        return ResponseEntity.ok().build();
    }
    
}

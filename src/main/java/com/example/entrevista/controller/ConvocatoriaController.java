package com.example.entrevista.controller;

import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.service.ConvocatoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;    // Solo empresas pueden crear convocatorias
    @PostMapping
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<Convocatoria> crear(@RequestBody Convocatoria convocatoria) {
        return ResponseEntity.ok(convocatoriaService.crearConvocatoria(convocatoria));
    }

    // Usuarios y empresas pueden ver detalles específicos
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<Convocatoria> buscarPorId(@PathVariable Long id) {
        return convocatoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Solo empresas pueden ver sus propias convocatorias
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<List<Convocatoria>> buscarPorEmpresa(@PathVariable Long empresaId) {
        List<Convocatoria> convocatorias = convocatoriaService.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(convocatorias);
    }

    // Solo usuarios pueden ver convocatorias activas (para postular)
    @GetMapping("/activas")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<Convocatoria>> listarActivas() {
        List<Convocatoria> convocatorias = convocatoriaService.listarActivas();
        return ResponseEntity.ok(convocatorias);
    }    // Solo empresas pueden eliminar sus convocatorias
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        convocatoriaService.eliminarConvocatoria(id);
        return ResponseEntity.ok().build();
    }
    
}

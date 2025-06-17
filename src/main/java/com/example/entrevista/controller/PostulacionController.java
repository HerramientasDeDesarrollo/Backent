package com.example.entrevista.controller;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.service.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;

    @PostMapping
    public ResponseEntity<Postulacion> crear(@RequestBody Postulacion postulacion) {
        return ResponseEntity.ok(postulacionService.crearPostulacion(postulacion));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Postulacion>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(postulacionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/convocatoria/{convocatoriaId}")
    public ResponseEntity<List<Postulacion>> listarPorConvocatoria(@PathVariable Long convocatoriaId) {
        return ResponseEntity.ok(postulacionService.listarPorConvocatoria(convocatoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Postulacion> buscarPorId(@PathVariable Long id) {
        return postulacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Nuevos endpoints para gestionar el estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Postulacion> actualizarEstado(
            @PathVariable Long id, 
            @RequestBody Map<String, String> cambioEstado) {
        
        String nuevoEstadoStr = cambioEstado.get("estado");
        if (nuevoEstadoStr == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            EstadoPostulacion nuevoEstado = EstadoPostulacion.valueOf(nuevoEstadoStr.toUpperCase());
            return ResponseEntity.ok(postulacionService.actualizarEstado(id, nuevoEstado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Postulacion>> listarPorEstado(@PathVariable String estado) {
        try {
            EstadoPostulacion estadoEnum = EstadoPostulacion.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(postulacionService.listarPorEstado(estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public ResponseEntity<List<Postulacion>> listarPorUsuarioYEstado(
            @PathVariable Long usuarioId,
            @PathVariable String estado) {
        
        try {
            EstadoPostulacion estadoEnum = EstadoPostulacion.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(postulacionService.listarPorUsuarioYEstado(usuarioId, estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/convocatoria/{convocatoriaId}/estado/{estado}")
    public ResponseEntity<List<Postulacion>> listarPorConvocatoriaYEstado(
            @PathVariable Long convocatoriaId,
            @PathVariable String estado) {
        
        try {
            EstadoPostulacion estadoEnum = EstadoPostulacion.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(postulacionService.listarPorConvocatoriaYEstado(convocatoriaId, estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint para actualizar una postulación completa
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Postulacion postulacion) {
        try {
            Postulacion postulacionActualizada = postulacionService.actualizarPostulacion(id, postulacion);
            return ResponseEntity.ok(postulacionActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar la postulación: " + e.getMessage()));
        }
    }

    // Endpoint para eliminar una postulación
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            postulacionService.eliminarPostulacion(id);
            return ResponseEntity.ok(Map.of("mensaje", "Postulación eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar la postulación: " + e.getMessage()));
        }
    }
}

package com.example.entrevista.controller;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.service.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;    // Solo usuarios pueden crear postulaciones
    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<Postulacion> crear(@RequestBody Postulacion postulacion) {
        return ResponseEntity.ok(postulacionService.crearPostulacion(postulacion));
    }

    // Solo usuarios pueden ver sus propias postulaciones
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<Postulacion>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(postulacionService.listarPorUsuario(usuarioId));
    }

    // Solo empresas pueden ver postulaciones de sus convocatorias
    @GetMapping("/convocatoria/{convocatoriaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<List<Postulacion>> listarPorConvocatoria(@PathVariable Long convocatoriaId) {
        return ResponseEntity.ok(postulacionService.listarPorConvocatoria(convocatoriaId));
    }

    // Usuarios pueden ver sus postulaciones, empresas pueden ver postulaciones de sus convocatorias
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<Postulacion> buscarPorId(@PathVariable Long id) {
        return postulacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
      // Solo empresas pueden actualizar estado (para mover candidatos entre fases)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('EMPRESA')") // Corregido: solo empresas
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
    
    // Solo empresas pueden filtrar por estado
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<List<Postulacion>> listarPorEstado(@PathVariable String estado) {
        try {
            EstadoPostulacion estadoEnum = EstadoPostulacion.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(postulacionService.listarPorEstado(estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    @PreAuthorize("hasRole('USUARIO')") // Agregado
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
    @PreAuthorize("hasRole('EMPRESA')") // Agregado
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
    @PreAuthorize("hasRole('EMPRESA') or hasRole('USUARIO')") // Agregado
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
    @PreAuthorize("hasRole('EMPRESA')") // Agregado - solo empresas pueden eliminar
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
    
    // Usuarios pueden iniciar su proceso de entrevista
    @PatchMapping("/{id}/iniciar-entrevista")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> iniciarEntrevista(@PathVariable Long id) {
        try {
            // Solo permitir cambio de PENDIENTE a EN_EVALUACION
            Postulacion postulacion = postulacionService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
            
            // Validar que esté en estado PENDIENTE
            if (postulacion.getEstado() != EstadoPostulacion.PENDIENTE) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se puede iniciar entrevista desde estado PENDIENTE. Estado actual: " + postulacion.getEstado()));
            }
            
            Postulacion postulacionActualizada = postulacionService.actualizarEstado(id, EstadoPostulacion.EN_EVALUACION);
            return ResponseEntity.ok(postulacionActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al iniciar entrevista: " + e.getMessage()));
        }
    }

    // Usuarios pueden completar su entrevista
    @PatchMapping("/{id}/completar-entrevista")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> completarEntrevista(@PathVariable Long id) {
        try {
            // Solo permitir cambio de EN_EVALUACION a COMPLETADA
            Postulacion postulacion = postulacionService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
            
            // Validar que esté en estado EN_EVALUACION
            if (postulacion.getEstado() != EstadoPostulacion.EN_EVALUACION) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se puede completar entrevista desde estado EN_EVALUACION. Estado actual: " + postulacion.getEstado()));
            }
            
            Postulacion postulacionActualizada = postulacionService.actualizarEstado(id, EstadoPostulacion.COMPLETADA);
            return ResponseEntity.ok(postulacionActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al completar entrevista: " + e.getMessage()));
        }
    }

    // Endpoint para marcar si las preguntas han sido generadas
    @PatchMapping("/{id}/marcar-preguntas-generadas")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> marcarPreguntasGeneradas(@PathVariable Long id, @RequestBody Map<String, Boolean> datos) {
        try {
            // Obtener el valor del parámetro generadas
            Boolean generadas = datos.get("generadas");
            if (generadas == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'generadas' es obligatorio"));
            }
            
            // Actualizar el campo preguntasGeneradas
            Postulacion postulacionActualizada = postulacionService.marcarPreguntasGeneradas(id, generadas);
            return ResponseEntity.ok(postulacionActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el estado de preguntas generadas: " + e.getMessage()));
        }
    }
}

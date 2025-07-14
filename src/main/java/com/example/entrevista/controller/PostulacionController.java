package com.example.entrevista.controller;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.service.PostulacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    private static final Logger logger = LoggerFactory.getLogger(PostulacionController.class);

    @Autowired
    private PostulacionService postulacionService;

    // Solo usuarios pueden crear postulaciones
    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> crear(@RequestBody Postulacion postulacion) {
        try {
            Postulacion nuevaPostulacion = postulacionService.crearPostulacion(postulacion);
            
            // Convertir a DTO para evitar problemas de serialización
            Map<String, Object> postulacionDTO = convertirADTO(nuevaPostulacion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", postulacionDTO,
                "mensaje", "Postulación creada exitosamente"
            ));
        } catch (Exception e) {
            logger.error("Error al crear postulación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error al crear la postulación: " + e.getMessage()
            ));
        }
    }

    // Usuarios pueden ver sus propias postulaciones, empresas pueden ver postulaciones de cualquier usuario
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId, Authentication authentication) {
        try {
            String emailAutenticado = authentication.getName();
            Collection<String> roles = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
            
            logger.debug("Usuario autenticado: {}, roles: {}, solicitando postulaciones del usuario ID: {}", emailAutenticado, roles, usuarioId);
            
            List<Postulacion> postulaciones = postulacionService.listarPorUsuario(usuarioId);
            
            // Si es un usuario (no empresa), verificar que solo pueda ver sus propias postulaciones
            if (roles.contains("ROLE_USUARIO") && !roles.contains("ROLE_EMPRESA")) {
                if (!postulaciones.isEmpty()) {
                    String emailPropietario = postulaciones.get(0).getUsuario().getEmail();
                    if (!emailAutenticado.equals(emailPropietario)) {
                        logger.warn("Usuario {} intentó acceder a postulaciones de usuario {}", emailAutenticado, emailPropietario);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                            "success", false,
                            "error", "No tienes permisos para ver estas postulaciones"
                        ));
                    }
                }
            }
            // Las empresas pueden ver postulaciones de cualquier usuario
            
            // Convertir a DTO para evitar problemas de serialización
            List<Map<String, Object>> postulacionesDTO = postulaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", postulacionesDTO,
                "total", postulacionesDTO.size(),
                "mensaje", postulaciones.isEmpty() ? "No tienes postulaciones aún" : "Postulaciones encontradas"
            ));
        } catch (Exception e) {
            logger.error("Error al obtener postulaciones del usuario {}: {}", usuarioId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    // Endpoint alternativo para que usuarios obtengan sus propias postulaciones por email
    @GetMapping("/mis-postulaciones")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> listarMisPostulaciones(Authentication authentication) {
        try {
            String emailAutenticado = authentication.getName();
            logger.debug("Usuario autenticado: {} solicitando sus propias postulaciones", emailAutenticado);
            
            List<Postulacion> postulaciones = postulacionService.listarPorEmail(emailAutenticado);
            
            // Convertir a DTO para evitar problemas de serialización
            List<Map<String, Object>> postulacionesDTO = postulaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", postulacionesDTO,
                "total", postulacionesDTO.size(),
                "mensaje", postulaciones.isEmpty() ? "No tienes postulaciones aún" : "Postulaciones encontradas"
            ));
        } catch (Exception e) {
            logger.error("Error al obtener mis postulaciones para usuario {}: {}", authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    // Método auxiliar para convertir Postulacion a DTO
    private Map<String, Object> convertirADTO(Postulacion postulacion) {
        Map<String, Object> dto = new HashMap<>();
        
        dto.put("id", postulacion.getId());
        dto.put("estado", postulacion.getEstado().name());
        dto.put("preguntasGeneradas", postulacion.isPreguntasGeneradas());
        dto.put("entrevistaSessionId", postulacion.getEntrevistaSessionId());
        
        // Usuario (datos básicos)
        if (postulacion.getUsuario() != null) {
            Map<String, Object> usuarioData = new HashMap<>();
            usuarioData.put("id", postulacion.getUsuario().getId());
            usuarioData.put("nombre", postulacion.getUsuario().getNombre());
            usuarioData.put("apellidoPaterno", postulacion.getUsuario().getApellidoPaterno());
            usuarioData.put("email", postulacion.getUsuario().getEmail());
            dto.put("usuario", usuarioData);
        }
        
        // Convocatoria (datos seguros sin lazy loading)
        if (postulacion.getConvocatoria() != null) {
            Map<String, Object> convocatoriaData = new HashMap<>();
            Convocatoria conv = postulacion.getConvocatoria();
            
            convocatoriaData.put("id", conv.getId());
            convocatoriaData.put("jobTitle", conv.getJobTitle());
            convocatoriaData.put("category", conv.getCategory() != null ? conv.getCategory().name() : null);
            convocatoriaData.put("experienceLevel", conv.getExperienceLevel() != null ? conv.getExperienceLevel().name() : null);
            convocatoriaData.put("workMode", conv.getWorkMode() != null ? conv.getWorkMode().name() : null);
            convocatoriaData.put("location", conv.getLocation());
            convocatoriaData.put("closingDate", conv.getClosingDate());
            convocatoriaData.put("activo", conv.isActivo());
            
            // Salario formateado
            if (conv.getSalaryMin() != null && conv.getSalaryMax() != null) {
                String currency = conv.getSalaryCurrency() != null ? conv.getSalaryCurrency() : "USD";
                convocatoriaData.put("salaryRange", String.format("%s %,.2f - %,.2f", 
                    currency, conv.getSalaryMin(), conv.getSalaryMax()));
            }
            
            // Empresa (solo datos básicos para evitar lazy loading)
            if (conv.getEmpresa() != null) {
                try {
                    convocatoriaData.put("empresaId", conv.getEmpresa().getId());
                    convocatoriaData.put("empresaNombre", conv.getEmpresa().getNombre());
                } catch (Exception e) {
                    // Si hay problema con lazy loading, usar valores por defecto
                    convocatoriaData.put("empresaId", null);
                    convocatoriaData.put("empresaNombre", "Cargando...");
                }
            }
            
            dto.put("convocatoria", convocatoriaData);
        }
        
        return dto;
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
    @PreAuthorize("hasRole('EMPRESA')")
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
    @PreAuthorize("hasRole('USUARIO')")
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
    @PreAuthorize("hasRole('EMPRESA')")
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
    @PreAuthorize("hasRole('EMPRESA') or hasRole('USUARIO')")
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
    @PreAuthorize("hasRole('EMPRESA')")
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
            
            // Crear o recuperar EntrevistaSession (idempotente)
            Long entrevistaSessionId = postulacionService.crearORecuperarEntrevistaSession(id);
            
            // Actualizar estado de la postulación
            Postulacion postulacionActualizada = postulacionService.actualizarEstado(id, EstadoPostulacion.EN_EVALUACION);
            
            // Convertir a DTO para evitar problemas de serialización
            Map<String, Object> postulacionDTO = convertirADTO(postulacionActualizada);
            
            // Preparar respuesta con entrevista_session_id
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("postulacion", postulacionDTO);
            respuesta.put("entrevista_session_id", entrevistaSessionId);
            respuesta.put("mensaje", "Entrevista iniciada exitosamente");
            
            return ResponseEntity.ok(respuesta);
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
            // Obtener el valor del parámetro preguntasGeneradas (corregido el nombre del campo)
            Boolean preguntasGeneradas = datos.get("preguntasGeneradas");
            if (preguntasGeneradas == null) {
                // Fallback para compatibilidad con nombres alternativos
                preguntasGeneradas = datos.get("generadas");
            }
            
            if (preguntasGeneradas == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'preguntasGeneradas' es obligatorio"));
            }
            
            // Actualizar el campo preguntasGeneradas
            Postulacion postulacionActualizada = postulacionService.marcarPreguntasGeneradas(id, preguntasGeneradas);
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

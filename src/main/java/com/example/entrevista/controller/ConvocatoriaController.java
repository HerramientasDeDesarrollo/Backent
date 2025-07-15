package com.example.entrevista.controller;

import com.example.entrevista.model.Category;
import com.example.entrevista.model.ExperienceLevel;
import com.example.entrevista.model.WorkMode;
import com.example.entrevista.DTO.ConvocatoriaCreateDTO;
import com.example.entrevista.DTO.ConvocatoriaResponseDTO;
import com.example.entrevista.service.ConvocatoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    private static final Logger logger = LoggerFactory.getLogger(ConvocatoriaController.class);

    @Autowired
    private ConvocatoriaService convocatoriaService;

    // Solo empresas pueden crear convocatorias (Nueva API)
    @PostMapping("/v2")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> crearV2(@RequestBody ConvocatoriaCreateDTO convocatoriaDTO) {
        try {
            logger.info("Creando nueva convocatoria: {}", convocatoriaDTO.getJobTitle());
            
            ConvocatoriaResponseDTO convocatoriaCreada = convocatoriaService.crearConvocatoria(convocatoriaDTO);
            
            logger.info("Convocatoria creada exitosamente con ID: {}", convocatoriaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convocatoriaCreada);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al crear convocatoria: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Datos de convocatoria inválidos: " + e.getMessage()
            ));
        } catch (RuntimeException e) {
            logger.error("Error al crear convocatoria: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error interno al crear convocatoria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Usuarios y empresas pueden ver detalles específicos (Nueva API)
    @GetMapping("/v2/{id}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> buscarPorIdV2(@PathVariable Long id) {
        try {
            return convocatoriaService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al buscar convocatoria {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Solo empresas pueden ver sus propias convocatorias (Nueva API)
    @GetMapping("/v2/empresa/{empresaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> buscarPorEmpresaV2(@PathVariable Long empresaId) {
        try {
            List<ConvocatoriaResponseDTO> convocatorias = convocatoriaService.buscarPorEmpresa(empresaId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "total", convocatorias.size()
            ));
        } catch (Exception e) {
            logger.error("Error al buscar convocatorias de empresa {}: {}", empresaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Solo usuarios pueden ver convocatorias activas (Nueva API)
    @GetMapping("/v2/activas")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<?> listarActivasV2() {
        try {
            List<ConvocatoriaResponseDTO> convocatorias = convocatoriaService.listarActivas();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "total", convocatorias.size()
            ));
        } catch (Exception e) {
            logger.error("Error al listar convocatorias activas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Filtro por categoría (modificado para aceptar números)
    @GetMapping("/v2/categoria/{categoriaNum}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> buscarPorCategoria(@PathVariable int categoriaNum) {
        try {
            Category categoria = Category.fromValue(categoriaNum);
            List<ConvocatoriaResponseDTO> convocatorias = convocatoriaService.buscarPorCategoria(categoria.name());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "categoria_numero", categoriaNum,
                "categoria_nombre", categoria.name(),
                "categoria_display", categoria.getDisplayName(),
                "total", convocatorias.size()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Categoría inválida: {}", categoriaNum);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Categoría inválida. Debe ser un número entre 1-8",
                "categorias_validas", Map.of(
                    "1", "TECHNOLOGY",
                    "2", "DESIGN", 
                    "3", "MARKETING",
                    "4", "SALES",
                    "5", "FINANCE",
                    "6", "OPERATIONS",
                    "7", "HUMAN_RESOURCES",
                    "8", "OTHER"
                )
            ));
        } catch (Exception e) {
            logger.error("Error al buscar por categoría {}: {}", categoriaNum, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Filtro por modalidad de trabajo (modificado para aceptar números)
    @GetMapping("/v2/modalidad/{modalidadNum}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> buscarPorModalidad(@PathVariable int modalidadNum) {
        try {
            WorkMode modalidad = WorkMode.fromValue(modalidadNum);
            List<ConvocatoriaResponseDTO> convocatorias = convocatoriaService.buscarPorModalidadTrabajo(modalidad.name());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "modalidad_numero", modalidadNum,
                "modalidad_nombre", modalidad.name(),
                "modalidad_display", modalidad.getDisplayName(),
                "total", convocatorias.size()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Modalidad inválida: {}", modalidadNum);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Modalidad inválida. Debe ser un número entre 1-3",
                "modalidades_validas", Map.of(
                    "1", "REMOTE",
                    "2", "ON_SITE",
                    "3", "HYBRID"
                )
            ));
        } catch (Exception e) {
            logger.error("Error al buscar por modalidad {}: {}", modalidadNum, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Filtro por nivel de experiencia (modificado para aceptar números)
    @GetMapping("/v2/experiencia/{experienciaNum}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> buscarPorExperiencia(@PathVariable int experienciaNum) {
        try {
            ExperienceLevel experiencia = ExperienceLevel.fromValue(experienciaNum);
            List<ConvocatoriaResponseDTO> convocatorias = convocatoriaService.buscarPorNivelExperiencia(experiencia.name());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "experiencia_numero", experienciaNum,
                "experiencia_nombre", experiencia.name(),
                "experiencia_display", experiencia.getDisplayName(),
                "total", convocatorias.size()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Nivel de experiencia inválido: {}", experienciaNum);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Nivel de experiencia inválido. Debe ser un número entre 1-4",
                "experiencias_validas", Map.of(
                    "1", "ENTRY (0-2 years)",
                    "2", "MID (3-5 years)",
                    "3", "SENIOR (6-8 years)",
                    "4", "LEAD (9+ years)"
                )
            ));
        } catch (Exception e) {
            logger.error("Error al buscar por experiencia {}: {}", experienciaNum, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Actualizar convocatoria
    @PutMapping("/v2/{id}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> actualizarV2(@PathVariable Long id, @RequestBody ConvocatoriaCreateDTO convocatoriaDTO) {
        try {
            ConvocatoriaResponseDTO convocatoriaActualizada = convocatoriaService.actualizarConvocatoria(id, convocatoriaDTO);
            return ResponseEntity.ok(convocatoriaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Datos inválidos: " + e.getMessage()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error al actualizar convocatoria {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // Solo empresas pueden eliminar sus convocatorias
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        convocatoriaService.eliminarConvocatoria(id);
        return ResponseEntity.ok().build();
    }
    
    // Endpoint para obtener metadatos y opciones disponibles
    @GetMapping("/v2/metadata")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> obtenerMetadata() {
        try {
            Map<String, Object> metadata = new HashMap<>();
            
            // Categorías con números
            Map<String, Object> categorias = new HashMap<>();
            for (Category category : Category.values()) {
                categorias.put(String.valueOf(category.getValue()), Map.of(
                    "nombre", category.name(),
                    "display", category.getDisplayName(),
                    "numero", category.getValue()
                ));
            }
            metadata.put("categories", categorias);
            
            // Niveles de experiencia con números
            Map<String, Object> experiencia = new HashMap<>();
            for (ExperienceLevel level : ExperienceLevel.values()) {
                experiencia.put(String.valueOf(level.getValue()), Map.of(
                    "nombre", level.name(),
                    "display", level.getDisplayName(),
                    "numero", level.getValue()
                ));
            }
            metadata.put("experience_levels", experiencia);
            
            // Modalidades de trabajo con números
            Map<String, Object> modalidades = new HashMap<>();
            for (WorkMode mode : WorkMode.values()) {
                modalidades.put(String.valueOf(mode.getValue()), Map.of(
                    "nombre", mode.name(),
                    "display", mode.getDisplayName(),
                    "numero", mode.getValue()
                ));
            }
            metadata.put("work_modes", modalidades);
            
            // Monedas comunes
            metadata.put("currencies", List.of("USD", "EUR", "MXN", "CAD", "GBP", "JPY"));
            
            // Rango de dificultad
            metadata.put("difficulty_range", Map.of("min", 1, "max", 10));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "metadata", metadata
            ));
        } catch (Exception e) {
            logger.error("Error obteniendo metadata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }
    
    // Obtener todas las convocatorias (Nueva API)
    @GetMapping("/v2")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    public ResponseEntity<?> listarTodasV2(
            @RequestParam(defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String experiencia) {
        try {
            List<ConvocatoriaResponseDTO> convocatorias;
            
            if (soloActivas) {
                convocatorias = convocatoriaService.listarActivas();
            } else {
                convocatorias = convocatoriaService.listarTodas();
            }
            
            // Aplicar filtros adicionales si se proporcionan
            if (categoria != null && !categoria.isEmpty()) {
                convocatorias = convocatorias.stream()
                    .filter(c -> c.getCategory().name().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
            }
            
            if (modalidad != null && !modalidad.isEmpty()) {
                convocatorias = convocatorias.stream()
                    .filter(c -> c.getWorkMode().name().equalsIgnoreCase(modalidad))
                    .collect(Collectors.toList());
            }
            
            if (experiencia != null && !experiencia.isEmpty()) {
                convocatorias = convocatorias.stream()
                    .filter(c -> c.getExperienceLevel().name().equalsIgnoreCase(experiencia))
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", convocatorias,
                "total", convocatorias.size(),
                "filtros", Map.of(
                    "soloActivas", soloActivas,
                    "categoria", categoria != null ? categoria : "todas",
                    "modalidad", modalidad != null ? modalidad : "todas",
                    "experiencia", experiencia != null ? experiencia : "todas"
                )
            ));
        } catch (Exception e) {
            logger.error("Error al listar todas las convocatorias: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    // === ENDPOINTS LEGACY PARA COMPATIBILIDAD ===
    
    // Legacy: Solo empresas pueden crear convocatorias
    @PostMapping
    @PreAuthorize("hasRole('EMPRESA')")
    @Deprecated
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> convocatoriaData) {
        try {
            // Convertir datos legacy al nuevo formato si es posible
            logger.warn("Usando endpoint legacy para crear convocatoria. Considere migrar a /v2");
            
            // Por ahora solo creamos una respuesta básica
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Este endpoint está deprecado. Use POST /api/convocatorias/v2 con el nuevo formato.",
                "new_endpoint", "/api/convocatorias/v2"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error: " + e.getMessage()
            ));
        }
    }

    // Legacy: Usuarios y empresas pueden ver detalles específicos
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or hasRole('EMPRESA')")
    @Deprecated
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        logger.warn("Usando endpoint legacy para buscar convocatoria {}. Considere migrar a /v2/{}", id, id);
        // Redirigir al nuevo endpoint internamente
        return buscarPorIdV2(id);
    }

    // Legacy: Solo empresas pueden ver sus propias convocatorias
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasRole('EMPRESA')")
    @Deprecated
    public ResponseEntity<?> buscarPorEmpresa(@PathVariable Long empresaId) {
        logger.warn("Usando endpoint legacy para buscar por empresa {}. Considere migrar a /v2/empresa/{}", empresaId, empresaId);
        // Redirigir al nuevo endpoint internamente
        return buscarPorEmpresaV2(empresaId);
    }

    // Legacy: Solo usuarios pueden ver convocatorias activas
    @GetMapping("/activas")
    @PreAuthorize("hasRole('USUARIO')")
    @Deprecated
    public ResponseEntity<?> listarActivas() {
        logger.warn("Usando endpoint legacy para listar activas. Considere migrar a /v2/activas");
        // Redirigir al nuevo endpoint internamente
        return listarActivasV2();
    }
    
}

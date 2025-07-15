package com.example.entrevista.service;

import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.DTO.ConvocatoriaCreateDTO;
import com.example.entrevista.DTO.ConvocatoriaResponseDTO;
import com.example.entrevista.repository.ConvocatoriaRepository;
import com.example.entrevista.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConvocatoriaService {

    private static final Logger logger = LoggerFactory.getLogger(ConvocatoriaService.class);

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;

    public ConvocatoriaResponseDTO crearConvocatoria(ConvocatoriaCreateDTO createDTO) {
        // Validaciones
        if (!createDTO.isValid()) {
            throw new IllegalArgumentException("Los datos de la convocatoria no son válidos");
        }
        
        // Buscar la empresa
        Empresa empresa = empresaRepository.findById(createDTO.getEmpresaId())
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + createDTO.getEmpresaId()));
        
        // Crear la entidad
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setJobTitle(createDTO.getJobTitle());
        convocatoria.setCategory(createDTO.getCategory());
        convocatoria.setJobDescription(createDTO.getJobDescription());
        convocatoria.setTechnicalRequirements(createDTO.getTechnicalRequirements());
        convocatoria.setExperienceLevel(createDTO.getExperienceLevel());
        convocatoria.setWorkMode(createDTO.getWorkMode());
        convocatoria.setLocation(createDTO.getLocation());
        convocatoria.setSalaryMin(createDTO.getSalaryMin());
        convocatoria.setSalaryMax(createDTO.getSalaryMax());
        convocatoria.setSalaryCurrency(createDTO.getSalaryCurrency() != null ? createDTO.getSalaryCurrency() : "USD");
        convocatoria.setBenefitsPerks(createDTO.getBenefitsPerks());
        convocatoria.setPublicationDate(createDTO.getPublicationDate());
        convocatoria.setClosingDate(createDTO.getClosingDate());
        convocatoria.setDificultad(createDTO.getDificultad());
        convocatoria.setActivo(true);
        convocatoria.setEmpresa(empresa);
        
        // Guardar
        Convocatoria savedConvocatoria = convocatoriaRepository.save(convocatoria);
        
        // Convertir a DTO de respuesta
        return convertToResponseDTO(savedConvocatoria);
    }

    public List<ConvocatoriaResponseDTO> listarTodas() {
        return convocatoriaRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<ConvocatoriaResponseDTO> buscarPorId(Long id) {
        return convocatoriaRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public void eliminarConvocatoria(Long id) {
        convocatoriaRepository.deleteById(id);
    }
    
    public List<ConvocatoriaResponseDTO> buscarPorEmpresa(Long empresaId) {
        return convocatoriaRepository.findByEmpresaId(empresaId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ConvocatoriaResponseDTO> listarActivas() {
        return convocatoriaRepository.findAll().stream()
                .filter(c -> c.isActivo() && c.getClosingDate() != null && c.getClosingDate().isAfter(LocalDate.now()))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<ConvocatoriaResponseDTO> buscarPorCategoria(String categoria) {
        // Buscar por categoría
        return convocatoriaRepository.findAll().stream()
                .filter(c -> c.getCategory() != null && c.getCategory().name().equalsIgnoreCase(categoria))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<ConvocatoriaResponseDTO> buscarPorModalidadTrabajo(String workMode) {
        return convocatoriaRepository.findAll().stream()
                .filter(c -> c.getWorkMode() != null && c.getWorkMode().name().equalsIgnoreCase(workMode))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<ConvocatoriaResponseDTO> buscarPorNivelExperiencia(String experienceLevel) {
        return convocatoriaRepository.findAll().stream()
                .filter(c -> c.getExperienceLevel() != null && c.getExperienceLevel().name().equalsIgnoreCase(experienceLevel))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public ConvocatoriaResponseDTO actualizarConvocatoria(Long id, ConvocatoriaCreateDTO updateDTO) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convocatoria no encontrada con ID: " + id));
        
        // Validaciones
        if (!updateDTO.isValid()) {
            throw new IllegalArgumentException("Los datos de actualización no son válidos");
        }
        
        // Actualizar campos
        convocatoria.setJobTitle(updateDTO.getJobTitle());
        convocatoria.setCategory(updateDTO.getCategory());
        convocatoria.setJobDescription(updateDTO.getJobDescription());
        convocatoria.setTechnicalRequirements(updateDTO.getTechnicalRequirements());
        convocatoria.setExperienceLevel(updateDTO.getExperienceLevel());
        convocatoria.setWorkMode(updateDTO.getWorkMode());
        convocatoria.setLocation(updateDTO.getLocation());
        convocatoria.setSalaryMin(updateDTO.getSalaryMin());
        convocatoria.setSalaryMax(updateDTO.getSalaryMax());
        convocatoria.setSalaryCurrency(updateDTO.getSalaryCurrency());
        convocatoria.setBenefitsPerks(updateDTO.getBenefitsPerks());
        convocatoria.setPublicationDate(updateDTO.getPublicationDate());
        convocatoria.setClosingDate(updateDTO.getClosingDate());
        convocatoria.setDificultad(updateDTO.getDificultad());
        
        Convocatoria updatedConvocatoria = convocatoriaRepository.save(convocatoria);
        return convertToResponseDTO(updatedConvocatoria);
    }
    
    public ConvocatoriaResponseDTO crearConvocatoriaV2(ConvocatoriaCreateDTO createDTO) {
        try {
            // Validaciones básicas
            if (!createDTO.isValid()) {
                throw new IllegalArgumentException("Los datos de la convocatoria no son válidos");
            }
            
            // Validar que los enums no son null (asumiendo que el DTO ya los convierte correctamente)
            if (createDTO.getCategory() == null) {
                throw new IllegalArgumentException("Categoría es requerida");
            }
            if (createDTO.getExperienceLevel() == null) {
                throw new IllegalArgumentException("Nivel de experiencia es requerido");
            }
            if (createDTO.getWorkMode() == null) {
                throw new IllegalArgumentException("Modalidad de trabajo es requerida");
            }

            // Buscar empresa
            Empresa empresa = empresaRepository.findById(createDTO.getEmpresaId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + createDTO.getEmpresaId()));

            // Crear convocatoria
            Convocatoria convocatoria = new Convocatoria();
            convocatoria.setJobTitle(createDTO.getJobTitle());
            convocatoria.setCategory(createDTO.getCategory());
            convocatoria.setJobDescription(createDTO.getJobDescription());
            convocatoria.setTechnicalRequirements(createDTO.getTechnicalRequirements());
            convocatoria.setExperienceLevel(createDTO.getExperienceLevel());
            convocatoria.setWorkMode(createDTO.getWorkMode());
            convocatoria.setLocation(createDTO.getLocation());
            convocatoria.setSalaryMin(createDTO.getSalaryMin());
            convocatoria.setSalaryMax(createDTO.getSalaryMax());
            convocatoria.setSalaryCurrency(createDTO.getSalaryCurrency() != null ? createDTO.getSalaryCurrency() : "USD");
            convocatoria.setBenefitsPerks(createDTO.getBenefitsPerks());
            convocatoria.setPublicationDate(createDTO.getPublicationDate());
            convocatoria.setClosingDate(createDTO.getClosingDate());
            convocatoria.setDificultad(createDTO.getDificultad());
            convocatoria.setEmpresa(empresa);
            convocatoria.setActivo(true);

            Convocatoria saved = convocatoriaRepository.save(convocatoria);
            return convertToResponseDTO(saved);
        } catch (Exception e) {
            logger.error("Error creando convocatoria: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear convocatoria: " + e.getMessage());
        }
    }

    // Método de conversión
    private ConvocatoriaResponseDTO convertToResponseDTO(Convocatoria convocatoria) {
        ConvocatoriaResponseDTO dto = new ConvocatoriaResponseDTO();
        dto.setId(convocatoria.getId());
        dto.setJobTitle(convocatoria.getJobTitle());
        dto.setCategory(convocatoria.getCategory());
        dto.setJobDescription(convocatoria.getJobDescription());
        dto.setTechnicalRequirements(convocatoria.getTechnicalRequirements());
        dto.setExperienceLevel(convocatoria.getExperienceLevel());
        dto.setWorkMode(convocatoria.getWorkMode());
        dto.setLocation(convocatoria.getLocation());
        dto.setSalaryMin(convocatoria.getSalaryMin());
        dto.setSalaryMax(convocatoria.getSalaryMax());
        dto.setSalaryCurrency(convocatoria.getSalaryCurrency());
        dto.setBenefitsPerks(convocatoria.getBenefitsPerks());
        dto.setPublicationDate(convocatoria.getPublicationDate());
        dto.setClosingDate(convocatoria.getClosingDate());
        dto.setActivo(convocatoria.isActivo());
        dto.setDificultad(convocatoria.getDificultad());
        dto.setEmpresaId(convocatoria.getEmpresa().getId());
        dto.setEmpresaNombre(convocatoria.getEmpresa().getNombre());
        
        // Compute calculated fields
        dto.computeFields();
        
        return dto;
    }
    
    // Método legacy para mantener compatibilidad
    @Deprecated
    public Convocatoria crearConvocatoria(Convocatoria convocatoria) {
        return convocatoriaRepository.save(convocatoria);
    }
}

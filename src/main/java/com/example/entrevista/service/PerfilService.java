package com.example.entrevista.service;

import com.example.entrevista.model.Perfil;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.repository.PerfilRepository;
import com.example.entrevista.repository.UsuarioRepository;
import com.example.entrevista.repository.EmpresaRepository;
import com.example.entrevista.DTO.PerfilResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class PerfilService {
    
    private static final Logger logger = LoggerFactory.getLogger(PerfilService.class);
    
    @Autowired
    private PerfilRepository perfilRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Value("${app.upload.profiles.dir:uploads/profiles}")
    private String uploadDir;
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;
    
    // Obtener perfil por usuario ID
    public Optional<PerfilResponseDTO> obtenerPerfilUsuario(Long usuarioId) {
        Optional<Perfil> perfil = perfilRepository.findByUsuarioId(usuarioId);
        if (perfil.isPresent()) {
            return Optional.of(convertirADTO(perfil.get()));
        }
        return Optional.empty();
    }
    
    // Obtener perfil por empresa ID
    public Optional<PerfilResponseDTO> obtenerPerfilEmpresa(Long empresaId) {
        Optional<Perfil> perfil = perfilRepository.findByEmpresaId(empresaId);
        if (perfil.isPresent()) {
            return Optional.of(convertirADTO(perfil.get()));
        }
        return Optional.empty();
    }
    
    // Crear perfil para usuario
    public PerfilResponseDTO crearPerfilUsuario(Long usuarioId) {
        if (perfilRepository.existsByUsuarioId(usuarioId)) {
            throw new RuntimeException("El usuario ya tiene un perfil");
        }
        
        Perfil perfil = new Perfil();
        perfil.setUsuarioId(usuarioId);
        perfil = perfilRepository.save(perfil);
        
        return convertirADTO(perfil);
    }
    
    // Crear perfil para empresa
    public PerfilResponseDTO crearPerfilEmpresa(Long empresaId) {
        if (perfilRepository.existsByEmpresaId(empresaId)) {
            throw new RuntimeException("La empresa ya tiene un perfil");
        }
        
        Perfil perfil = new Perfil();
        perfil.setEmpresaId(empresaId);
        perfil = perfilRepository.save(perfil);
        
        return convertirADTO(perfil);
    }
    
    // Subir imagen de perfil
    public PerfilResponseDTO subirImagenPerfil(Long perfilId, MultipartFile archivo) throws IOException {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        
        // Validar archivo
        validarArchivo(archivo);
        
        // Generar nombre único
        String nombreArchivo = generarNombreArchivo(perfil, archivo.getOriginalFilename());
        
        // Crear directorio si no existe
        Path directorioPath = Paths.get(uploadDir);
        if (!Files.exists(directorioPath)) {
            Files.createDirectories(directorioPath);
        }
        
        // Guardar archivo
        Path archivoPath = directorioPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), archivoPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Actualizar perfil con URL
        String imagenUrl = "/uploads/profiles/" + nombreArchivo;
        perfil.setImagenUrl(imagenUrl);
        perfil = perfilRepository.save(perfil);
        
        logger.info("Imagen subida exitosamente: {}", imagenUrl);
        return convertirADTO(perfil);
    }
    
    // Eliminar imagen de perfil
    public PerfilResponseDTO eliminarImagenPerfil(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        
        // Eliminar archivo físico si existe
        if (perfil.getImagenUrl() != null) {
            try {
                String nombreArchivo = perfil.getImagenUrl().substring(perfil.getImagenUrl().lastIndexOf("/") + 1);
                Path archivoPath = Paths.get(uploadDir, nombreArchivo);
                Files.deleteIfExists(archivoPath);
            } catch (IOException e) {
                logger.warn("No se pudo eliminar el archivo físico: {}", e.getMessage());
            }
        }
        
        // Limpiar URL en BD
        perfil.setImagenUrl(null);
        perfil = perfilRepository.save(perfil);
        
        return convertirADTO(perfil);
    }
    
    // Convertir entidad a DTO con datos enriquecidos
    private PerfilResponseDTO convertirADTO(Perfil perfil) {
        PerfilResponseDTO dto = new PerfilResponseDTO();
        dto.setId(perfil.getId());
        dto.setUsuarioId(perfil.getUsuarioId());
        dto.setEmpresaId(perfil.getEmpresaId());
        dto.setImagenUrl(perfil.getImagenUrl());
        dto.setTieneImagen(perfil.tieneImagen());
        dto.setFechaCreacion(perfil.getFechaCreacion());
        dto.setFechaActualizacion(perfil.getFechaActualizacion());
        dto.setTipoPerfil(perfil.esUsuario() ? "USUARIO" : "EMPRESA");
        
        // Calcular URL completa
        if (perfil.getImagenUrl() != null) {
            dto.setImagenUrlCompleta(baseUrl + perfil.getImagenUrl());
        }
        
        // Obtener nombre completo dinámicamente
        if (perfil.esUsuario()) {
            Optional<Usuario> usuario = usuarioRepository.findById(perfil.getUsuarioId());
            if (usuario.isPresent()) {
                Usuario u = usuario.get();
                String nombreCompleto = String.format("%s %s %s", 
                    u.getNombre(), 
                    u.getApellidoPaterno(), 
                    u.getApellidoMaterno() != null ? u.getApellidoMaterno() : ""
                ).trim();
                dto.setNombreCompleto(nombreCompleto);
            }
        } else {
            Optional<Empresa> empresa = empresaRepository.findById(perfil.getEmpresaId());
            if (empresa.isPresent()) {
                dto.setNombreCompleto(empresa.get().getNombre());
            }
        }
        
        return dto;
    }
    
    // Validar archivo de imagen
    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new RuntimeException("Archivo vacío");
        }
        
        // Validar tamaño (5MB máximo)
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Archivo muy grande. Máximo 5MB");
        }
        
        // Validar tipo
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten imágenes");
        }
    }
    
    // Generar nombre único para archivo
    private String generarNombreArchivo(Perfil perfil, String nombreOriginal) {
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }
        
        String prefix = perfil.esUsuario() ? "user" : "empresa";
        Long id = perfil.esUsuario() ? perfil.getUsuarioId() : perfil.getEmpresaId();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return String.format("%s_%d_%s%s", prefix, id, timestamp, extension);
    }
}

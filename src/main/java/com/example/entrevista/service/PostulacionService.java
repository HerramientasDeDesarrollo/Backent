package com.example.entrevista.service;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostulacionService {

    @Autowired
    private PostulacionRepository postulacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inyecta el repositorio de Usuario
    
    public Postulacion crearPostulacion(Postulacion postulacion) {
        // Aseguramos que toda nueva postulación comienza en estado PENDIENTE
        if (postulacion.getEstado() == null) {
            postulacion.setEstado(EstadoPostulacion.PENDIENTE);
        }

        // --- INICIO DE MODIFICACIÓN CLAVE ---
        // Obtener el ID del usuario del objeto Postulacion recibido
        Long usuarioId = postulacion.getUsuario().getId();
        
        // Cargar el objeto Usuario completo desde la base de datos
        // Esto asegura que el campo 'rol' y otros campos estén populados
        Usuario usuarioCompleto = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        // Asignar el objeto Usuario completamente cargado a la Postulacion
        postulacion.setUsuario(usuarioCompleto);
        // --- FIN DE MODIFICACIÓN CLAVE ---

        return postulacionRepository.save(postulacion);
    }

    public List<Postulacion> listarPorUsuario(Long usuarioId) {
        return postulacionRepository.findByUsuarioId(usuarioId);
    }

    public List<Postulacion> listarPorConvocatoria(Long convocatoriaId) {
        return postulacionRepository.findByConvocatoriaId(convocatoriaId);
    }

    public Optional<Postulacion> buscarPorId(Long id) {
        return postulacionRepository.findById(id);
    }
    
    // Nuevos métodos para gestionar el estado
    public Postulacion actualizarEstado(Long id, EstadoPostulacion nuevoEstado) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));

        // Validar transición de estado
        if (!esTransicionValida(postulacion.getEstado(), nuevoEstado)) {
            throw new RuntimeException("Transición de estado inválida de " + postulacion.getEstado() + " a " + nuevoEstado);
        }

        postulacion.setEstado(nuevoEstado);
        return postulacionRepository.save(postulacion);
    }
    
    public List<Postulacion> listarPorEstado(EstadoPostulacion estado) {
        return postulacionRepository.findByEstado(estado);
    }
    
    public List<Postulacion> listarPorUsuarioYEstado(Long usuarioId, EstadoPostulacion estado) {
        return postulacionRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }
    
    public List<Postulacion> listarPorConvocatoriaYEstado(Long convocatoriaId, EstadoPostulacion estado) {
        return postulacionRepository.findByConvocatoriaIdAndEstado(convocatoriaId, estado);
    }
    
    // Método para actualizar una postulación completa
    public Postulacion actualizarPostulacion(Long id, Postulacion postulacion) {
        return postulacionRepository.findById(id)
                .map(postulacionExistente -> {
                    // Mantener la ID original
                    postulacion.setId(id);
                    // Asegurarse de que no modificamos el estado de generación de preguntas
                    postulacion.setPreguntasGeneradas(postulacionExistente.isPreguntasGeneradas());
                    return postulacionRepository.save(postulacion);
                })
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
    }

    // Método para eliminar una postulación
    public void eliminarPostulacion(Long id) {
        // Verificar si existe la postulación antes de eliminarla
        if (postulacionRepository.existsById(id)) {
            postulacionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Postulación no encontrada con ID: " + id);
        }
    }
    
    // Método privado para validar transiciones (opcional)
    private boolean esTransicionValida(EstadoPostulacion estadoActual, EstadoPostulacion nuevoEstado) {
        return switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoPostulacion.EN_EVALUACION;
            case EN_EVALUACION -> nuevoEstado == EstadoPostulacion.COMPLETADA;
            case COMPLETADA -> false; // No se puede cambiar desde completada
        };
    }

    // En PostulacionService.java añade este nuevo método
    public Postulacion marcarPreguntasGeneradas(Long id, boolean generadas) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
        
        postulacion.setPreguntasGeneradas(generadas);
        return postulacionRepository.save(postulacion);
    }
}

package com.example.entrevista.service;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.repository.PostulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostulacionService {

    @Autowired
    private PostulacionRepository postulacionRepository;

    public Postulacion crearPostulacion(Postulacion postulacion) {
        // Aseguramos que toda nueva postulación comienza en estado PENDIENTE
        if (postulacion.getEstado() == null) {
            postulacion.setEstado(EstadoPostulacion.PENDIENTE);
        }
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
    public Postulacion actualizarEstado(Long postulacionId, EstadoPostulacion nuevoEstado) {
        return postulacionRepository.findById(postulacionId)
                .map(postulacion -> {
                    postulacion.setEstado(nuevoEstado);
                    return postulacionRepository.save(postulacion);
                })
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + postulacionId));
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
}

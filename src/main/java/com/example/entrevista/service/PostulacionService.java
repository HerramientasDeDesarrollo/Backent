package com.example.entrevista.service;

import com.example.entrevista.model.Entrevista;
import com.example.entrevista.model.Postulacion;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.repository.PostulacionRepository;
import com.example.entrevista.repository.UsuarioRepository;
import com.example.entrevista.repository.EntrevistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostulacionService {

    @Autowired
    private PostulacionRepository postulacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntrevistaRepository entrevistaRepository;

    public List<Postulacion> findAll() {
        return postulacionRepository.findAll();
    }

    public Optional<Postulacion> findById(Long id) {
        return postulacionRepository.findById(id);
    }

    public Postulacion save(Postulacion postulacion) {
        return postulacionRepository.save(postulacion);
    }

    public void deleteById(Long id) {
        postulacionRepository.deleteById(id);
    }

    public void crearPostulacion(Long usuarioId, Long entrevistaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Entrevista entrevista = entrevistaRepository.findById(entrevistaId)
            .orElseThrow(() -> new RuntimeException("Entrevista no encontrada"));

        Optional<Postulacion> existente = postulacionRepository
            .findByUsuarioIdAndEntrevistaIdAndCiclo(usuario.getId(), entrevista.getId(), entrevista.getCiclo());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una postulación para este usuario en este ciclo de la entrevista.");
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setUsuario(usuario);
        postulacion.setEntrevista(entrevista);
        postulacion.setFechaPostulacion(new Date());
        postulacion.setEstado("pendiente");
        postulacion.setCiclo(entrevista.getCiclo());
        postulacionRepository.save(postulacion);
    }

    public List<Postulacion> obtenerHistorialPorUsuarioYEntrevista(Long usuarioId, Long entrevistaId) {
        return postulacionRepository.findByUsuarioIdAndEntrevistaId(usuarioId, entrevistaId);
    }
}

package com.example.entrevista.service;

import com.example.entrevista.model.Respuesta;
import com.example.entrevista.repository.RespuestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RespuestaService {

    @Autowired
    private RespuestaRepository respuestaRepository;

    public List<Respuesta> findAll() {
        return respuestaRepository.findAll();
    }

    public Optional<Respuesta> findById(Long id) {
        return respuestaRepository.findById(id);
    }

    public Respuesta save(Respuesta respuesta) {
        return respuestaRepository.save(respuesta);
    }

    public void deleteById(Long id) {
        respuestaRepository.deleteById(id);
    }
}

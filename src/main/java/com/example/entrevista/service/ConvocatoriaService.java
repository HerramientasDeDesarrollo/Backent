package com.example.entrevista.service;

import com.example.entrevista.model.Convocatoria;
import com.example.entrevista.repository.ConvocatoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConvocatoriaService {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    public Convocatoria crearConvocatoria(Convocatoria convocatoria) {
        return convocatoriaRepository.save(convocatoria);
    }

    public List<Convocatoria> listarTodas() {
        return convocatoriaRepository.findAll();
    }

    public Optional<Convocatoria> buscarPorId(Long id) {
        return convocatoriaRepository.findById(id);
    }

    public void eliminarConvocatoria(Long id) {
        convocatoriaRepository.deleteById(id);
    }
}

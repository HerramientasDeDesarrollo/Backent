package com.example.entrevista.service;

import com.example.entrevista.model.Entrevista;
import com.example.entrevista.repository.EntrevistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntrevistaService {

    @Autowired
    private EntrevistaRepository entrevistaRepository;

    public List<Entrevista> findAll() {
        return entrevistaRepository.findAll();
    }

    public Optional<Entrevista> findById(Long id) {
        return entrevistaRepository.findById(id);
    }

    public Entrevista save(Entrevista entrevista) {
        return entrevistaRepository.save(entrevista);
    }

    public void deleteById(Long id) {
        entrevistaRepository.deleteById(id);
    }
}

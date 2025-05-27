package com.example.entrevista.service;

import com.example.entrevista.model.Resultado;
import com.example.entrevista.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultadoService {

    @Autowired
    private ResultadoRepository resultadoRepository;

    public List<Resultado> findAll() {
        return resultadoRepository.findAll();
    }

    public Optional<Resultado> findById(Long id) {
        return resultadoRepository.findById(id);
    }

    public Resultado save(Resultado resultado) {
        return resultadoRepository.save(resultado);
    }

    public void deleteById(Long id) {
        resultadoRepository.deleteById(id);
    }
}

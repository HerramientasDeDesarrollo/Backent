package com.example.entrevista.service;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    public Empresa crearEmpresa(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    public Optional<Empresa> buscarPorId(Long id) {
        return empresaRepository.findById(id);
    }

    public Optional<Empresa> buscarPorEmail(String email) {
        return empresaRepository.findByEmail(email);
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }
}

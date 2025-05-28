package com.example.entrevista.service;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    public Optional<Empresa> findById(Long id) {
        return empresaRepository.findById(id);
    }

    public Empresa save(Empresa empresa) {
        empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
        return empresaRepository.save(empresa);
    }

    public void deleteById(Long id) {
        empresaRepository.deleteById(id);
    }
}

package com.example.entrevista.service;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.repository.EmpresaRepository;
import com.example.entrevista.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Empresa crearEmpresa(Empresa empresa) {
        // VALIDAR SI EMAIL YA EXISTE COMO EMPRESA
        if (empresaRepository.findByEmail(empresa.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado como empresa");
        }
        
        // VALIDAR SI EMAIL YA EXISTE COMO USUARIO
        if (usuarioRepository.findByEmail(empresa.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado como usuario");
        }
        
        // Hashear la contraseña antes de guardar
        empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
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

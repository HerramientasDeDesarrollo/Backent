package com.example.entrevista.Service;

import com.example.entrevista.Model.UsuarioModel;
import com.example.entrevista.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Create
    public UsuarioModel registrar(UsuarioModel usuario) {
        return usuarioRepository.save(usuario);
    }

    // Read
    public List<UsuarioModel> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<UsuarioModel> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // Update
    public UsuarioModel actualizar(Long id, UsuarioModel usuario) {
        if (usuarioRepository.existsById(id)) {
            usuario.setId(id);
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    // Delete
    public boolean eliminar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Login
    public UsuarioModel login(String username, String password) {
        return usuarioRepository.findByUsername(username)
            .filter(user -> user.getPassword().equals(password))
            .orElse(null);
    }
}
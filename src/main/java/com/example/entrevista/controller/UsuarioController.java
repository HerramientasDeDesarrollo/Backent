package com.example.entrevista.controller;

import com.example.entrevista.model.Usuario;
import com.example.entrevista.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Solo ADMIN puede ver todos los usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Usuario> findAll() {
        return usuarioService.findAll();
    }

    // Solo ADMIN puede ver usuarios por id
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Optional<Usuario> findById(@PathVariable Long id) {
        return usuarioService.findById(id);
    }

    // Registro público de usuario (sin protección)
    @PostMapping("/registro")
    public Usuario registrar(@RequestBody Usuario usuario) {
        return usuarioService.save(usuario);
    }

    // Solo ADMIN puede eliminar usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        usuarioService.deleteById(id);
    }
}

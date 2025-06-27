package com.example.entrevista.controller;

import com.example.entrevista.DTO.UsuarioCreateDTO;
import com.example.entrevista.DTO.UsuarioResponseDTO;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Permitir creación de usuarios (registro público)
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody UsuarioCreateDTO usuarioCreateDTO) {
        // Convertir DTO a entidad
        Usuario usuario = new Usuario();
        usuario.setEmail(usuarioCreateDTO.getEmail());
        usuario.setNombre(usuarioCreateDTO.getNombre());
        usuario.setApellidoPaterno(usuarioCreateDTO.getApellidoPaterno());
        usuario.setApellidoMaterno(usuarioCreateDTO.getApellidoMaterno());
        usuario.setPassword(usuarioCreateDTO.getPassword());
        usuario.setRol(usuarioCreateDTO.getRol());
        
        Usuario usuarioCreado = usuarioService.crearUsuario(usuario);
        
        // Convertir entidad a DTO de respuesta
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuarioCreado.getId());
        response.setEmail(usuarioCreado.getEmail());
        response.setNombre(usuarioCreado.getNombre());
        response.setApellidoPaterno(usuarioCreado.getApellidoPaterno());
        response.setApellidoMaterno(usuarioCreado.getApellidoMaterno());
        response.setRol(usuarioCreado.getRol());
        
        return ResponseEntity.ok(response);
    }

    // Solo usuarios pueden ver su propio perfil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') and #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    UsuarioResponseDTO response = new UsuarioResponseDTO();
                    response.setId(usuario.getId());
                    response.setEmail(usuario.getEmail());
                    response.setNombre(usuario.getNombre());
                    response.setApellidoPaterno(usuario.getApellidoPaterno());
                    response.setApellidoMaterno(usuario.getApellidoMaterno());
                    response.setRol(usuario.getRol());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Para autenticación - permitir búsqueda por email (mantener Usuario para autenticación)
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Solo admins pueden listar todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioResponseDTO> response = usuarios.stream()
                .map(usuario -> {
                    UsuarioResponseDTO dto = new UsuarioResponseDTO();
                    dto.setId(usuario.getId());
                    dto.setEmail(usuario.getEmail());
                    dto.setNombre(usuario.getNombre());
                    dto.setApellidoPaterno(usuario.getApellidoPaterno());
                    dto.setApellidoMaterno(usuario.getApellidoMaterno());
                    dto.setRol(usuario.getRol());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}

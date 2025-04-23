package com.example.entrevista.Controller;

import com.example.entrevista.DTO.LoginDTO;
import com.example.entrevista.Model.UsuarioModel;
import com.example.entrevista.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Create
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioModel usuario) {
        try {
            UsuarioModel nuevoUsuario = authService.registrar(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }

    // Read
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioModel>> obtenerTodos() {
        return ResponseEntity.ok(authService.obtenerTodos());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return authService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody UsuarioModel usuario) {
        UsuarioModel actualizado = authService.actualizar(id, usuario);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (authService.eliminar(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {
        UsuarioModel usuario = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.badRequest().body("Credenciales inválidas");
    }
}
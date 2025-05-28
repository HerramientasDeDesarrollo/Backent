package com.example.entrevista.controller;

import com.example.entrevista.DTO.AuthRequest;
import com.example.entrevista.DTO.AuthResponse;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.util.JwtUtil;
import com.example.entrevista.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.example.entrevista.repository.EmpresaRepository empresaRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Credenciales inválidas");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority());

        Map<String, String> response = new HashMap<>();
        response.put("jwt", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-empresa")
    public ResponseEntity<?> loginEmpresa(@RequestBody AuthRequest request) {
        Empresa empresa = empresaRepository.findByCorreo(request.getCorreo())
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        if (!passwordEncoder.matches(request.getPassword(), empresa.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        // Genera el JWT para la empresa (puedes usar el email y un rol EMPRESA)
        String token = jwtUtil.generateToken(empresa.getCorreo(), "ROLE_EMPRESA");
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

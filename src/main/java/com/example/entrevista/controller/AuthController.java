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
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo y contraseña son obligatorios"));
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            System.out.println("userDetails: " + userDetails);
            
            // Get the full authority including "ROLE_" prefix
            String authority = userDetails.getAuthorities().iterator().next().getAuthority();
            
            // Pass the full authority to generateToken - the method will handle the prefix
            String token = jwtUtil.generateToken(userDetails.getUsername(), authority);
            System.out.println("token generado: " + token);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            System.out.println("Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/login-empresa")
    public ResponseEntity<?> loginEmpresa(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo y contraseña son obligatorios"));
        }
        Empresa empresa = empresaRepository.findByEmail(request.getEmail())
            .orElse(null);
        if (empresa == null || !passwordEncoder.matches(request.getPassword(), empresa.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
        // Include the full ROLE_ prefix
        String token = jwtUtil.generateToken(empresa.getEmail(), "ROLE_EMPRESA");
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

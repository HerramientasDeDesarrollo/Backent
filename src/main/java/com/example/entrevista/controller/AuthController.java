package com.example.entrevista.controller;

import com.example.entrevista.DTO.AuthRequest;
import com.example.entrevista.DTO.AuthResponse;
import com.example.entrevista.util.JwtUtil;
import com.example.entrevista.service.CustomUserDetailsService;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.model.Empresa;
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
            
            // Obtener información adicional del usuario o empresa
            Usuario usuario = userDetailsService.findUsuarioByEmail(request.getEmail());
            Empresa empresa = null;
            
            String token;
            if (usuario != null) {
                // Es un usuario
                token = jwtUtil.generateToken(
                    userDetails.getUsername(), 
                    authority,
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno()
                );
            } else {
                // Es una empresa
                empresa = userDetailsService.findEmpresaByEmail(request.getEmail());
                if (empresa != null) {
                    token = jwtUtil.generateToken(
                        userDetails.getUsername(), 
                        authority,
                        empresa.getId(),
                        empresa.getNombre(),
                        null, // Las empresas no tienen apellidos
                        null
                    );
                } else {
                    // Fallback al método original
                    token = jwtUtil.generateToken(userDetails.getUsername(), authority);
                }
            }
            
            System.out.println("token generado: " + token);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            System.out.println("Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }
}

package com.example.entrevista.controller;

import com.example.entrevista.DTO.AuthRequest;
import com.example.entrevista.DTO.AuthResponse;
import com.example.entrevista.util.JwtUtil;
import com.example.entrevista.service.CustomUserDetailsService;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.model.Empresa;
import com.example.entrevista.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            AuthResponse response;
            
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
                response = new AuthResponse(
                    token,
                    "USUARIO",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno(),
                    usuario.getEmail()
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
                    response = new AuthResponse(
                        token,
                        "EMPRESA",
                        empresa.getId(),
                        empresa.getNombre(),
                        null, // Las empresas no tienen apellidos
                        null,
                        empresa.getEmail()
                    );
                } else {
                    // Fallback al método original
                    token = jwtUtil.generateToken(userDetails.getUsername(), authority);
                    response = new AuthResponse(token);
                }
            }
            
            System.out.println("token generado: " + token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            System.out.println("Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                
                Map<String, Object> userInfo = Map.of(
                    "id", userPrincipal.getId(),
                    "email", userPrincipal.getEmail(),
                    "nombre", userPrincipal.getNombre() != null ? userPrincipal.getNombre() : "",
                    "apellidoPaterno", userPrincipal.getApellidoPaterno() != null ? userPrincipal.getApellidoPaterno() : "",
                    "apellidoMaterno", userPrincipal.getApellidoMaterno() != null ? userPrincipal.getApellidoMaterno() : "",
                    "nombreCompleto", userPrincipal.getNombreCompleto(),
                    "userType", userPrincipal.getUserType(),
                    "roles", authentication.getAuthorities()
                );
                
                return ResponseEntity.ok(userInfo);
            }
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
}

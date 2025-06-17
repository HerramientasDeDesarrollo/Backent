package com.example.entrevista.service;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.model.Usuario;
import com.example.entrevista.repository.UsuarioRepository;
import com.example.entrevista.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar usuario por email
        Usuario user = usuarioRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRol() != null ? user.getRol().name() : "USUARIO")
                .build();
        }
        // Buscar empresa por email
        Empresa empresa = empresaRepository.findByEmail(email).orElse(null);
        if (empresa != null) {
            return org.springframework.security.core.userdetails.User
                .withUsername(empresa.getEmail())
                .password(empresa.getPassword())
                .roles(empresa.getRol() != null ? empresa.getRol().name() : "EMPRESA")
                .build();
        }
        throw new UsernameNotFoundException("No existe usuario o empresa con email: " + email);
    }

    public Usuario saveUser(Usuario user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return usuarioRepository.save(user);
    }
}

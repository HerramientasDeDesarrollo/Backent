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

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByCorreo(correo);
        if (user != null) {
            return user;
        }
        // Buscar en empresa
        Optional<Empresa> empresaOpt = empresaRepository.findByCorreo(correo);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Adaptador para UserDetails
            return org.springframework.security.core.userdetails.User
                .withUsername(empresa.getCorreo())
                .password(empresa.getPassword())
                .roles("EMPRESA")
                .build();
        }
        throw new UsernameNotFoundException("No existe usuario o empresa con correo: " + correo);
    }

    public Usuario saveUser(Usuario user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return usuarioRepository.save(user);
    }
}

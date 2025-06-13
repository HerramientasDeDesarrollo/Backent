package com.example.entrevista.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Usuario;

public interface UsuarioRepository extends JpaRepository <Usuario, Long>{

    Optional<Usuario> findByEmail(String email);

}

package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
            Usuario findByCorreo(String correo);

}

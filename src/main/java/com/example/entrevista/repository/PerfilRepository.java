package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.entrevista.model.Perfil;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    
    // Buscar perfil por usuario ID
    Optional<Perfil> findByUsuarioId(Long usuarioId);
    
    // Buscar perfil por empresa ID
    Optional<Perfil> findByEmpresaId(Long empresaId);
    
    // Verificar si usuario ya tiene perfil
    boolean existsByUsuarioId(Long usuarioId);
    
    // Verificar si empresa ya tiene perfil
    boolean existsByEmpresaId(Long empresaId);
}

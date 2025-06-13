package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Convocatoria;
import java.util.List;

public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    // Find convocatorias by empresa id
    List<Convocatoria> findByEmpresaId(Long empresaId);
    
    // Find active convocatorias
    List<Convocatoria> findByActivoTrue();
}

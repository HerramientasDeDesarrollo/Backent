package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entrevista.model.Convocatoria;
import java.util.List;

public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    // Find convocatorias by empresa id
    List<Convocatoria> findByEmpresaId(Long empresaId);
    
    // Find active convocatorias using explicit query to handle numeric boolean representation
    @Query("SELECT c FROM Convocatoria c WHERE c.activo = true")
    List<Convocatoria> findByActivoTrue();
    
    // Alternative method with more explicit SQL native query
    @Query(value = "SELECT * FROM convocatoria WHERE activo = 1", nativeQuery = true)
    List<Convocatoria> findActiveConvocatorias();
}

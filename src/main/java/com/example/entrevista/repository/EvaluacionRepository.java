package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import com.example.entrevista.model.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    List<Evaluacion> findByPostulacionId(Long postulacionId);
    List<Evaluacion> findByPreguntaId(Long preguntaId);
    
    // Asumiendo que entrevistaId se refiere a un usuario que realiza entrevistas
    @Query("SELECT e FROM Evaluacion e WHERE e.postulacion.convocatoria.empresa.id = :entrevistaId")
    List<Evaluacion> findByEntrevistaId(@Param("entrevistaId") Long entrevistaId);

    // Métodos de conteo optimizados
    long countByPostulacionId(Long postulacionId);

    long countByPreguntaId(Long preguntaId);

    @Query("SELECT COUNT(e) FROM Evaluacion e WHERE e.postulacion.id = :postulacionId AND " +
           "e.claridadEstructura IS NOT NULL AND e.dominioTecnico IS NOT NULL AND " +
           "e.pertinencia IS NOT NULL AND e.comunicacionSeguridad IS NOT NULL AND " +
           "e.porcentajeObtenido IS NOT NULL AND e.evaluacionCompleta IS NOT NULL AND " +
           "e.evaluacionCompleta != ''")
    long countEvaluacionesCompletas(@Param("postulacionId") Long postulacionId);

    @Query("SELECT COUNT(e) FROM Evaluacion e WHERE e.postulacion.id = :postulacionId AND " +
           "(e.claridadEstructura IS NULL OR e.dominioTecnico IS NULL OR " +
           "e.pertinencia IS NULL OR e.comunicacionSeguridad IS NULL OR " +
           "e.porcentajeObtenido IS NULL OR e.evaluacionCompleta IS NULL OR " +
           "e.evaluacionCompleta = '')")
    long countEvaluacionesIncompletas(@Param("postulacionId") Long postulacionId);
}

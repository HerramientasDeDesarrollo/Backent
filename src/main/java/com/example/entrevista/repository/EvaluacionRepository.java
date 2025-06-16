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
}

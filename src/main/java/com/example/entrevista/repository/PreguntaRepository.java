package com.example.entrevista.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entrevista.model.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByConvocatoriaId(Long convocatoriaId);

    List<Pregunta> findByPostulacionId(Long postulacionId);

    List<Pregunta> findByConvocatoriaIdAndPostulacionId(Long convocatoriaId, Long postulacionId);

    List<Pregunta> findByTextoPregunta(String textoPregunta);

    // Métodos de conteo optimizados
    long countByPostulacionId(Long postulacionId);

    long countByConvocatoriaId(Long convocatoriaId);

    @Query("SELECT COUNT(p) FROM Pregunta p WHERE p.textoPregunta IS NOT NULL AND p.textoPregunta != '' AND p.postulacion.id = :postulacionId")
    long countPreguntasCompletasByPostulacionId(@Param("postulacionId") Long postulacionId);

}

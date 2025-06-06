package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entrevista.model.Pregunta;
import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByEntrevistaId(Long entrevistaId);
    List<Pregunta> findByPostulacionId(Long postulacionId);
    List<Pregunta> findByEntrevistaIdAndPostulacionId(Long entrevistaId, Long postulacionId);
}

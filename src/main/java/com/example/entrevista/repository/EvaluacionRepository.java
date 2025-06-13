package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.entrevista.model.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    List<Evaluacion> findByPostulacionId(Long postulacionId);
    List<Evaluacion> findByPreguntaId(Long preguntaId);
}

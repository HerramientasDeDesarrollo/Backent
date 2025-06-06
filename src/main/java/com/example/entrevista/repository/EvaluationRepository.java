package com.example.entrevista.repository;

import com.example.entrevista.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluacion, Long> {
}

package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Respuesta;

public interface EvaluationRepository extends JpaRepository<Respuesta, Long>{

    
}

package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Convocatoria;

public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
}

package com.example.entrevista.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entrevista.Model.PreguntaModel;

@Repository
public interface PreguntaRepository extends JpaRepository<PreguntaModel, Long> {
}


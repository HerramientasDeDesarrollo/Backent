package com.example.entrevista.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByConvocatoriaId(Long convocatoriaId);

    List<Pregunta> findByPostulacionId(Long postulacionId);

    List<Pregunta> findByConvocatoriaIdAndPostulacionId(Long convocatoriaId, Long postulacionId);

    List<Pregunta> findByTextoPregunta(String textoPregunta);

}

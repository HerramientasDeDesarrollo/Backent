package com.example.entrevista.repository;

import com.example.entrevista.model.Entrevista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrevistaRepository extends JpaRepository<Entrevista, Long> {
    
    // Buscar entrevistas por rango de puntuación
    List<Entrevista> findByPuntuacionFinalBetween(Double puntuacionMinima, Double puntuacionMaxima);
    
    // Buscar entrevistas con puntuación mayor a un valor
    List<Entrevista> findByPuntuacionFinalGreaterThanEqual(Double puntuacionMinima);
    
    // Buscar entrevistas que contengan cierta fortaleza (case insensitive)
    @Query("SELECT e FROM Entrevista e WHERE LOWER(e.fortalezas) LIKE LOWER(CONCAT('%', :fortaleza, '%'))")
    List<Entrevista> findByFortalezasContainingIgnoreCase(@Param("fortaleza") String fortaleza);
    
    // Buscar la última entrevista realizada
    Optional<Entrevista> findFirstByOrderByIdDesc();
    
    // Contar entrevistas por rango de puntuación
    Long countByPuntuacionFinalBetween(Double puntuacionMinima, Double puntuacionMaxima);
    
    // Buscar entrevistas que necesitan mejoras en cierta área
    @Query("SELECT e FROM Entrevista e WHERE LOWER(e.oportunidadesMejora) LIKE LOWER(CONCAT('%', :area, '%'))")
    List<Entrevista> findByAreaDeMejora(@Param("area") String area);
    
    // Obtener entrevistas con puntuación superior al promedio
    @Query("SELECT e FROM Entrevista e WHERE e.puntuacionFinal > (SELECT AVG(e2.puntuacionFinal) FROM Entrevista e2)")
    List<Entrevista> findEntrevistasConPuntuacionSuperiorAlPromedio();
}
package com.example.entrevista.repository;

import com.example.entrevista.model.EntrevistaSession;
import com.example.entrevista.model.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntrevistaSessionRepository extends JpaRepository<EntrevistaSession, Long> {
    
    // Buscar por postulación
    Optional<EntrevistaSession> findByPostulacionId(Long postulacionId);
    
    // Buscar por postulación y estado
    Optional<EntrevistaSession> findByPostulacionIdAndEstadoSesion(Long postulacionId, EstadoSesion estado);
    
    // Verificar si existe sesión para una postulación
    boolean existsByPostulacionId(Long postulacionId);
    
    // Buscar sesiones por estado
    List<EntrevistaSession> findByEstadoSesion(EstadoSesion estado);
    
    // Buscar sesiones por usuario (a través de postulación)
    @Query("SELECT es FROM EntrevistaSession es WHERE es.postulacion.usuario.id = :usuarioId")
    List<EntrevistaSession> findByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    // Buscar sesiones por empresa (a través de postulación -> convocatoria)
    @Query("SELECT es FROM EntrevistaSession es WHERE es.postulacion.convocatoria.empresa.id = :empresaId")
    List<EntrevistaSession> findByEmpresaId(@Param("empresaId") Long empresaId);
    
    // Buscar sesiones completadas
    List<EntrevistaSession> findByEsCompletadaTrue();
    
    // Buscar sesiones no completadas
    List<EntrevistaSession> findByEsCompletadaFalse();
    
    // Buscar sesiones con actividad reciente
    @Query("SELECT es FROM EntrevistaSession es WHERE es.fechaUltimaActividad >= :fecha")
    List<EntrevistaSession> findByFechaUltimaActividadAfter(@Param("fecha") LocalDateTime fecha);
    
    // Buscar sesiones inactivas (para limpieza)
    @Query("SELECT es FROM EntrevistaSession es WHERE es.fechaUltimaActividad < :fecha AND es.esCompletada = false")
    List<EntrevistaSession> findSesionesInactivas(@Param("fecha") LocalDateTime fecha);
    
    // Estadísticas rápidas
    @Query("SELECT COUNT(es) FROM EntrevistaSession es WHERE es.estadoSesion = :estado")
    long countByEstadoSesion(@Param("estado") EstadoSesion estado);
    
    @Query("SELECT COUNT(es) FROM EntrevistaSession es WHERE es.esCompletada = true")
    long countCompletadas();
    
    @Query("SELECT COUNT(es) FROM EntrevistaSession es WHERE es.esCompletada = false")
    long countNoCompletadas();
    
    // Progreso promedio
    @Query("SELECT AVG(es.progresoCompletado) FROM EntrevistaSession es WHERE es.esCompletada = false")
    Double getProgresoPromedio();
}

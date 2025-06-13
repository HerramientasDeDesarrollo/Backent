package com.example.entrevista.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.EstadoPostulacion;
import com.example.entrevista.model.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findByUsuarioId(Long usuarioId);

    List<Postulacion> findByConvocatoriaId(Long convocatoriaId);
    
    List<Postulacion> findByEstado(EstadoPostulacion estado);
    
    List<Postulacion> findByUsuarioIdAndEstado(Long usuarioId, EstadoPostulacion estado);
    
    List<Postulacion> findByConvocatoriaIdAndEstado(Long convocatoriaId, EstadoPostulacion estado);
}

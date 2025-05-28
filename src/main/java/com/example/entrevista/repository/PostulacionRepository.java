package com.example.entrevista.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion,Long>{

	Optional<Postulacion> findByUsuarioIdAndEntrevistaIdAndCiclo(Long usuarioId, Long entrevistaId, int ciclo);
	
	List<Postulacion> findByUsuarioIdAndEntrevistaId(Long usuarioId, Long entrevistaId);
}

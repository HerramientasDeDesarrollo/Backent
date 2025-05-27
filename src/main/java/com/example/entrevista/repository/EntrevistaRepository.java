package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Entrevista;

public interface EntrevistaRepository extends JpaRepository<Entrevista,Long>{

}

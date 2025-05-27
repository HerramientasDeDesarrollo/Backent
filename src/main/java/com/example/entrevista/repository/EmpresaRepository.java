package com.example.entrevista.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entrevista.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

}

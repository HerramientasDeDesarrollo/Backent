package com.example.entrevista.controller;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    // Cualquier usuario autenticado puede ver todas las empresas
    @GetMapping
    public List<Empresa> findAll() {
        return empresaService.findAll();
    }

    // Cualquier usuario autenticado puede ver empresa por id
    @GetMapping("/{id}")
    public Optional<Empresa> findById(@PathVariable Long id) {
        return empresaService.findById(id);
    }

    // Solo ADMIN puede crear empresas
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Empresa save(@RequestBody Empresa empresa) {
        return empresaService.save(empresa);
    }

    // Solo ADMIN puede eliminar empresas
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        empresaService.deleteById(id);
    }
}

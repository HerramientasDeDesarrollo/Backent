package com.example.entrevista.controller;

import com.example.entrevista.model.Empresa;
import com.example.entrevista.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;    // Permitir creación de empresas (registro público)
    @PostMapping
    public ResponseEntity<Empresa> crear(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(empresaService.crearEmpresa(empresa));
    }

    // Solo empresas pueden ver su propio perfil, usuarios pueden ver info básica
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('USUARIO')")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        return empresaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Para autenticación - permitir búsqueda por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Empresa> buscarPorEmail(@PathVariable String email) {
        return empresaService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Solo admins pueden listar todas las empresas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Empresa>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }
}

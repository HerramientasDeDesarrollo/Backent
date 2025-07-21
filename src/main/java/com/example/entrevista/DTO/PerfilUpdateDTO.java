package com.example.entrevista.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUpdateDTO {
    
    // Solo necesitamos esto para actualizar la imagen
    // El resto de datos (nombre, etc.) se obtienen de Usuario/Empresa
    private String imagenUrl;
}

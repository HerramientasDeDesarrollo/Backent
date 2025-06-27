package com.example.entrevista.DTO;

import com.example.entrevista.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String email;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Rol rol;
}

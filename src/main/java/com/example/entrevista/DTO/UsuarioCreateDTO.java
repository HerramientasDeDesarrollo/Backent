package com.example.entrevista.DTO;

import com.example.entrevista.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {
    private String email;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Date nacimiento;
    private int telefono;
    private String password;
    private Rol rol;
}

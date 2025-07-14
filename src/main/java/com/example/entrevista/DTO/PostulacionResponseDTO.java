package com.example.entrevista.DTO;

import com.example.entrevista.model.EstadoPostulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostulacionResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private Long convocatoriaId;
    private String convocatoriaTitulo;
    private String empresaNombre;
    private EstadoPostulacion estado;
    private boolean preguntasGeneradas;
    private Long entrevistaSessionId;
}

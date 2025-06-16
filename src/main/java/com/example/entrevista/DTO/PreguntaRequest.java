package com.example.entrevista.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaRequest {
    private Long idPostulacion; // Para identificar la postulación
    
    // Estos campos se calcularán internamente
    private String puesto;     // Se obtendrá de la convocatoria asociada a la postulación
    private Long idConvocatoria; // Se obtendrá de la postulación
}

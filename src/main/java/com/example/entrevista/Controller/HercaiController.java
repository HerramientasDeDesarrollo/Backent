package com.example.entrevista.Controller;

import com.example.entrevista.Service.HercaiService;
import com.example.entrevista.Model.PreguntaModel;
import com.example.entrevista.Repository.PreguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hercai")
public class HercaiController {

    @Autowired
    private HercaiService hercaiService;

    @Autowired
    private PreguntaRepository preguntaRepo;

    @PostMapping("/generar")
    public ResponseEntity<?> generarPreguntas() {
        try {
            List<Map<String, String>> preguntasConResultados = hercaiService.generarPreguntasRaw();

            List<PreguntaModel> guardadas = preguntasConResultados.stream().map(p -> {
                PreguntaModel preg = new PreguntaModel();
                preg.setContenido(p.get("pregunta"));
                return preguntaRepo.save(preg);
            }).toList();

            return ResponseEntity.ok(preguntasConResultados);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/todas")
    public List<PreguntaModel> listar() {
        return preguntaRepo.findAll();
    }
}

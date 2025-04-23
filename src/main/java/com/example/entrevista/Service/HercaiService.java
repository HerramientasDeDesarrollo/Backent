package com.example.entrevista.Service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class HercaiService {

    private static final List<String> PREGUNTAS = Arrays.asList(
        "¿Cómo manejas la gestión del tiempo en un proyecto de desarrollo?",
        "¿Cuáles son los principales desafíos que has enfrentado como desarrollador frontend?",
        "¿Qué es el DOM y cómo se relaciona con el trabajo en JavaScript?",
        "¿Cuáles son las principales diferencias entre React y Angular?",
        "¿Cómo optimizas el rendimiento de una aplicación web?",
        "¿Qué es un estado en React y cómo lo gestionas?",
        "¿Cuál es la diferencia entre CSS Flexbox y Grid?",
        "¿Qué es una API RESTful y cómo la consumes desde el frontend?",
        "¿Cómo gestionas las solicitudes HTTP en aplicaciones JavaScript?",
        "¿Cómo asegurarías que tu código sea escalable y mantenible?",
        "¿Qué herramientas usas para la depuración de código JavaScript?",
        "¿Qué es el control de versiones y qué herramientas usas para ello?",
        "¿Cómo te mantienes al día con las nuevas tecnologías en el desarrollo frontend?",
        "¿Cuáles son las mejores prácticas para la accesibilidad web?",
        "¿Qué técnicas utilizas para hacer que un sitio web sea responsivo?",
        "¿Cómo manejas los errores y excepciones en el frontend?",
        "¿Qué es un Progressive Web App (PWA) y qué beneficios ofrece?",
        "¿Qué es un Webpack y cómo ayuda en el desarrollo frontend?",
        "¿Qué es un 'Single Page Application' (SPA) y cuáles son sus ventajas?",
        "¿Cómo manejas la seguridad en las aplicaciones frontend?"
    );

    public List<Map<String, String>> generarPreguntasRaw() {
        Random random = new Random();
        List<Map<String, String>> preguntasConResultados = new ArrayList<>();

        List<String> preguntasSeleccionadas = new ArrayList<>();
        while (preguntasSeleccionadas.size() < 10) {
            String preguntaAleatoria = PREGUNTAS.get(random.nextInt(PREGUNTAS.size()));
            if (!preguntasSeleccionadas.contains(preguntaAleatoria)) {
                preguntasSeleccionadas.add(preguntaAleatoria);
            }
        }

        for (String pregunta : preguntasSeleccionadas) {
            String resultado = random.nextBoolean() ? "correcto" : "incorrecto";
            preguntasConResultados.add(Map.of("pregunta", pregunta, "resultado", resultado));
        }

        return preguntasConResultados;
    }
}

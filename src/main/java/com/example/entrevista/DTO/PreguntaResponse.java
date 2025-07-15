package com.example.entrevista.DTO;

import java.util.List;
import java.util.Map;

public class PreguntaResponse {

    private boolean success;
    private List<PreguntaDTO> questions;
    private String mensaje;
    
    // Nuevos campos para tracking de respuestas
    private int totalPreguntas;
    private int preguntasRespondidas;
    private int preguntasPendientes;
    private double progresoRespuestas; // porcentaje de respuestas completadas
    private Long entrevistaSessionId;
    private Map<String, Boolean> estadoRespuestas; // pregunta_id -> respondida (true/false)

    // Getters y setters existentes
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<PreguntaDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<PreguntaDTO> questions) {
        this.questions = questions;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    // Nuevos getters y setters para tracking
    public int getTotalPreguntas() {
        return totalPreguntas;
    }

    public void setTotalPreguntas(int totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }

    public int getPreguntasRespondidas() {
        return preguntasRespondidas;
    }

    public void setPreguntasRespondidas(int preguntasRespondidas) {
        this.preguntasRespondidas = preguntasRespondidas;
    }

    public int getPreguntasPendientes() {
        return preguntasPendientes;
    }

    public void setPreguntasPendientes(int preguntasPendientes) {
        this.preguntasPendientes = preguntasPendientes;
    }

    public double getProgresoRespuestas() {
        return progresoRespuestas;
    }

    public void setProgresoRespuestas(double progresoRespuestas) {
        this.progresoRespuestas = progresoRespuestas;
    }

    public Long getEntrevistaSessionId() {
        return entrevistaSessionId;
    }

    public void setEntrevistaSessionId(Long entrevistaSessionId) {
        this.entrevistaSessionId = entrevistaSessionId;
    }

    public Map<String, Boolean> getEstadoRespuestas() {
        return estadoRespuestas;
    }

    public void setEstadoRespuestas(Map<String, Boolean> estadoRespuestas) {
        this.estadoRespuestas = estadoRespuestas;
    }

    public static class PreguntaDTO {
        private Long id; // ID de la pregunta
        private String type;
        private String typeReadable; // Versión legible del tipo
        private String question;
        private int score;  // porcentaje asignado a la pregunta
        
        // Nuevos campos para tracking
        private boolean respondida; // si la pregunta ha sido respondida
        private boolean evaluada; // si la pregunta ha sido evaluada
        private String respuesta; // texto de la respuesta (si existe)
        private String fechaRespuesta; // cuándo fue respondida

        // Getters y setters existentes
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
        public String getTypeReadable() {
            return typeReadable;
        }

        public void setTypeReadable(String typeReadable) {
            this.typeReadable = typeReadable;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
        
        // Nuevos getters y setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isRespondida() {
            return respondida;
        }

        public void setRespondida(boolean respondida) {
            this.respondida = respondida;
        }

        public boolean isEvaluada() {
            return evaluada;
        }

        public void setEvaluada(boolean evaluada) {
            this.evaluada = evaluada;
        }

        public String getRespuesta() {
            return respuesta;
        }

        public void setRespuesta(String respuesta) {
            this.respuesta = respuesta;
        }

        public String getFechaRespuesta() {
            return fechaRespuesta;
        }

        public void setFechaRespuesta(String fechaRespuesta) {
            this.fechaRespuesta = fechaRespuesta;
        }
    }
}
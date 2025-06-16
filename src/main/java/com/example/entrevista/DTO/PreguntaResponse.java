package com.example.entrevista.DTO;

public class PreguntaResponse {

    private boolean success;
    private java.util.List<PreguntaDTO> questions;
    private String mensaje; // Añadido para el mensaje

    // Getters y setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public java.util.List<PreguntaDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(java.util.List<PreguntaDTO> questions) {
        this.questions = questions;
    }

    public String getMensaje() { // Getter para el mensaje
        return mensaje;
    }

    public void setMensaje(String mensaje) { // Setter para el mensaje
        this.mensaje = mensaje;
    }

    public static class PreguntaDTO {
        private String type;
        private String typeReadable; // Versión legible del tipo
        private String question;
        private int score;  // porcentaje asignado a la pregunta

        // Getters y setters
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
    }
}
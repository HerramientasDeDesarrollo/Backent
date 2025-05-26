package com.example.entrevista.DTO;

public class PreguntaResponse {

    private boolean success;
    private java.util.List<PreguntaDTO> questions;

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

    public static class PreguntaDTO {
        private String type;
        private String question;
        private int score;  // porcentaje asignado a la pregunta

        // Getters y setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

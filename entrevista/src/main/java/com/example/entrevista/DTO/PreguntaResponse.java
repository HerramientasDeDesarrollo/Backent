package com.example.entrevista.DTO;

import java.util.List;
import java.util.Objects;

public class PreguntaResponse {
    private boolean success;
    private List<String> questions;

    public PreguntaResponse() {}

    public PreguntaResponse(boolean success, List<String> questions) {
        this.success = success;
        this.questions = questions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "PreguntaResponse{" +
                "success=" + success +
                ", questions=" + questions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntaResponse that = (PreguntaResponse) o;
        return success == that.success && Objects.equals(questions, that.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, questions);
    }
}
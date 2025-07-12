package com.example.entrevista.DTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResultadosResponse {
    
    private boolean success;
    private String mensaje;
    private Long usuarioId;
    private Long convocatoriaId;
    private String tituloConvocatoria;
    private Date fechaEvaluacion;
    private Double puntajeFinal;
    private Map<String, Double> resumenPorCriterio;
    private List<String> fortalezas;
    private List<String> oportunidadesMejora;
    private List<DetallePregunta> detallePreguntas;
    private String error;

    // Constructores
    public ResultadosResponse() {}

    public ResultadosResponse(boolean success, String mensaje) {
        this.success = success;
        this.mensaje = mensaje;
    }

    // Métodos estáticos para crear respuestas específicas
    public static ResultadosResponse success() {
        return new ResultadosResponse(true, null);
    }

    public static ResultadosResponse success(String mensaje) {
        return new ResultadosResponse(true, mensaje);
    }

    public static ResultadosResponse error(String mensaje) {
        ResultadosResponse response = new ResultadosResponse(false, null);
        response.setError(mensaje);
        return response;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getConvocatoriaId() {
        return convocatoriaId;
    }

    public void setConvocatoriaId(Long convocatoriaId) {
        this.convocatoriaId = convocatoriaId;
    }

    public String getTituloConvocatoria() {
        return tituloConvocatoria;
    }

    public void setTituloConvocatoria(String tituloConvocatoria) {
        this.tituloConvocatoria = tituloConvocatoria;
    }

    public Date getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Date fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Double getPuntajeFinal() {
        return puntajeFinal;
    }

    public void setPuntajeFinal(Double puntajeFinal) {
        this.puntajeFinal = puntajeFinal;
    }

    public Map<String, Double> getResumenPorCriterio() {
        return resumenPorCriterio;
    }

    public void setResumenPorCriterio(Map<String, Double> resumenPorCriterio) {
        this.resumenPorCriterio = resumenPorCriterio;
    }

    public List<String> getFortalezas() {
        return fortalezas;
    }

    public void setFortalezas(List<String> fortalezas) {
        this.fortalezas = fortalezas;
    }

    public List<String> getOportunidadesMejora() {
        return oportunidadesMejora;
    }

    public void setOportunidadesMejora(List<String> oportunidadesMejora) {
        this.oportunidadesMejora = oportunidadesMejora;
    }

    public List<DetallePregunta> getDetallePreguntas() {
        return detallePreguntas;
    }

    public void setDetallePreguntas(List<DetallePregunta> detallePreguntas) {
        this.detallePreguntas = detallePreguntas;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Clase interna para detalles de preguntas
    public static class DetallePregunta {
        private Integer numero;
        private String tipo;
        private String pregunta;
        private String respuestaUsuario;
        private DetalleEvaluacion evaluacion;

        // Constructores
        public DetallePregunta() {}

        public DetallePregunta(Integer numero, String tipo, String pregunta, String respuestaUsuario, DetalleEvaluacion evaluacion) {
            this.numero = numero;
            this.tipo = tipo;
            this.pregunta = pregunta;
            this.respuestaUsuario = respuestaUsuario;
            this.evaluacion = evaluacion;
        }

        // Getters y Setters
        public Integer getNumero() {
            return numero;
        }

        public void setNumero(Integer numero) {
            this.numero = numero;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getPregunta() {
            return pregunta;
        }

        public void setPregunta(String pregunta) {
            this.pregunta = pregunta;
        }

        public String getRespuestaUsuario() {
            return respuestaUsuario;
        }

        public void setRespuestaUsuario(String respuestaUsuario) {
            this.respuestaUsuario = respuestaUsuario;
        }

        public DetalleEvaluacion getEvaluacion() {
            return evaluacion;
        }

        public void setEvaluacion(DetalleEvaluacion evaluacion) {
            this.evaluacion = evaluacion;
        }
    }

    // Clase interna para detalles de evaluación
    public static class DetalleEvaluacion {
        private Double claridadEstructura;
        private Double dominioTecnico;
        private Double pertinencia;
        private Double comunicacionSeguridad;
        private Double puntuacionFinal;
        private List<String> fortalezas;
        private List<String> oportunidadesMejora;

        // Constructores
        public DetalleEvaluacion() {}

        public DetalleEvaluacion(Double claridadEstructura, Double dominioTecnico, Double pertinencia, 
                                Double comunicacionSeguridad, Double puntuacionFinal, 
                                List<String> fortalezas, List<String> oportunidadesMejora) {
            this.claridadEstructura = claridadEstructura;
            this.dominioTecnico = dominioTecnico;
            this.pertinencia = pertinencia;
            this.comunicacionSeguridad = comunicacionSeguridad;
            this.puntuacionFinal = puntuacionFinal;
            this.fortalezas = fortalezas;
            this.oportunidadesMejora = oportunidadesMejora;
        }

        // Getters y Setters
        public Double getClaridadEstructura() {
            return claridadEstructura;
        }

        public void setClaridadEstructura(Double claridadEstructura) {
            this.claridadEstructura = claridadEstructura;
        }

        public Double getDominioTecnico() {
            return dominioTecnico;
        }

        public void setDominioTecnico(Double dominioTecnico) {
            this.dominioTecnico = dominioTecnico;
        }

        public Double getPertinencia() {
            return pertinencia;
        }

        public void setPertinencia(Double pertinencia) {
            this.pertinencia = pertinencia;
        }

        public Double getComunicacionSeguridad() {
            return comunicacionSeguridad;
        }

        public void setComunicacionSeguridad(Double comunicacionSeguridad) {
            this.comunicacionSeguridad = comunicacionSeguridad;
        }

        public Double getPuntuacionFinal() {
            return puntuacionFinal;
        }

        public void setPuntuacionFinal(Double puntuacionFinal) {
            this.puntuacionFinal = puntuacionFinal;
        }

        public List<String> getFortalezas() {
            return fortalezas;
        }

        public void setFortalezas(List<String> fortalezas) {
            this.fortalezas = fortalezas;
        }

        public List<String> getOportunidadesMejora() {
            return oportunidadesMejora;
        }

        public void setOportunidadesMejora(List<String> oportunidadesMejora) {
            this.oportunidadesMejora = oportunidadesMejora;
        }
    }
}

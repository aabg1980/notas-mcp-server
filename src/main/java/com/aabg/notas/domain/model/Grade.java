package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa la calificación de un estudiante en una prueba.
 * Escala estricta de 0.0 a 5.0.
 */
public class Grade {

    public static final double MIN_SCORE = 0.0;
    public static final double MAX_SCORE = 5.0;

    private final Long id;
    private final Long studentId;
    private final Long evaluationId;
    private final double score;
    private final String feedback;
    private final LocalDateTime gradedAt;

    public Grade(Long id, Long studentId, Long evaluationId, double score, String feedback, LocalDateTime gradedAt) {
        if (studentId == null) {
            throw new BusinessRuleException("La calificación debe estar asociada a un estudiante");
        }
        if (evaluationId == null) {
            throw new BusinessRuleException("La calificación debe estar asociada a una prueba/evaluación");
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new BusinessRuleException(
                    String.format("La calificación debe estar en el rango de %.1f a %.1f (recibida: %.2f)", MIN_SCORE, MAX_SCORE, score));
        }

        this.id = id;
        this.studentId = studentId;
        this.evaluationId = evaluationId;
        this.score = Math.round(score * 100.0) / 100.0; // Redondeo a 2 decimales
        this.feedback = feedback != null ? feedback.trim() : "";
        this.gradedAt = gradedAt != null ? gradedAt : LocalDateTime.now();
    }

    public static Grade createNew(Long studentId, Long evaluationId, double score, String feedback) {
        return new Grade(null, studentId, evaluationId, score, feedback, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grade grade)) return false;
        return Objects.equals(studentId, grade.studentId) && Objects.equals(evaluationId, grade.evaluationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, evaluationId);
    }
}

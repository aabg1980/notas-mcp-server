package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad interna del agregado Evaluation que representa la calificación de un
 * estudiante en una prueba académica (una "nota").
 *
 * <p>El estudiante es referenciado únicamente por su identidad ({@code studentId}),
 * nunca por objeto: es una referencia cruzada hacia el agregado Student. De igual
 * forma, la evaluación a la que pertenece la nota es el propio agregado contenedor.</p>
 *
 * <p>Invariante protegido: la nota es un {@link GradeScore} (escala 0.0 a 5.0) y
 * por tanto siempre válida por construcción. Dentro de una misma evaluación no
 * puede existir más de una nota por estudiante (garantizado por la raíz del agregado).</p>
 */
public class Grade {

    private final Long id;
    private final Long studentId;
    private GradeScore score;
    private String feedback;
    private final LocalDateTime gradedAt;

    private Grade(Long id, Long studentId, GradeScore score, String feedback, LocalDateTime gradedAt) {
        validateStudentId(studentId);

        this.id = id;
        this.studentId = studentId;
        this.score = Objects.requireNonNull(score, "La calificación es obligatoria");
        this.feedback = feedback == null ? "" : feedback.trim();
        this.gradedAt = gradedAt != null ? gradedAt : LocalDateTime.now();
    }

    /** Factory method para registrar una nota nueva (aún sin identidad persistida). */
    public static Grade create(Long studentId, GradeScore score, String feedback) {
        return new Grade(null, studentId, score, feedback, LocalDateTime.now());
    }

    /** Factory method para reconstruir una nota desde la persistencia. */
    public static Grade rehydrate(Long id, Long studentId, GradeScore score, String feedback, LocalDateTime gradedAt) {
        return new Grade(id, studentId, score, feedback, gradedAt);
    }

    /**
     * Actualiza la calificación y la retroalimentación de la nota, dejando constancia
     * del momento de la última modificación.
     */
    void updateScore(GradeScore newScore, String newFeedback) {
        Objects.requireNonNull(newScore, "La nueva calificación es obligatoria");
        this.score = newScore;
        this.feedback = newFeedback == null ? "" : newFeedback.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public GradeScore getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    /** Indica si la nota aprueba la evaluación (>= 3.0). */
    public boolean isPassing() {
        return score.isPassing();
    }

    private static void validateStudentId(Long studentId) {
        if (studentId == null) {
            throw new BusinessRuleException("La calificación debe estar asociada a un estudiante");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Grade grade)) {
            return false;
        }
        return Objects.equals(studentId, grade.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(studentId);
    }
}

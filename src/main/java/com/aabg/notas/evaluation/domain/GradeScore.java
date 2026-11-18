package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object inmutable (Java record) que representa la calificación numérica de un estudiante
 * en la escala académica colombiana de 0.0 a 5.0.
 *
 * <p>Invariante protegido: el valor siempre está en el rango [0.0, 5.0], con escala de 2 decimales.
 * Es imposible construir una calificación fuera del rango permitido por el negocio.</p>
 */
public record GradeScore(BigDecimal value) {

    /** Nota mínima permitida (reprobado total). */
    public static final BigDecimal MIN = new BigDecimal("0.00");

    /** Nota máxima permitida (excelente). */
    public static final BigDecimal MAX = new BigDecimal("5.00");

    /** Nota mínima de aprobación de una asignatura. */
    public static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("3.00");

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo GradeScore existente sea válido por definición.
     */
    public GradeScore {
        Objects.requireNonNull(value, "La calificación es obligatoria");
        if (value.compareTo(MIN) < 0 || value.compareTo(MAX) > 0) {
            throw new BusinessRuleException(String.format(
                    "La calificación debe estar entre %s y %s (recibida: %s)",
                    MIN.toPlainString(), MAX.toPlainString(), value));
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Factory method a partir de un valor numérico. */
    public static GradeScore of(BigDecimal value) {
        return new GradeScore(value);
    }

    /** Factory method a partir de una representación textual (ej. "4.5"). */
    public static GradeScore of(String value) {
        return new GradeScore(new BigDecimal(value));
    }

    /** Indica si la nota aprueba la asignatura (>= 3.0). */
    public boolean isPassing() {
        return value.compareTo(APPROVAL_THRESHOLD) >= 0;
    }

    /** Promedio inmutable entre esta nota y {@code other} (usado para estadísticas del agregado). */
    public GradeScore averageWith(GradeScore other) {
        Objects.requireNonNull(other, "La calificación a promediar es obligatoria");
        BigDecimal average = value.add(other.value)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        return new GradeScore(average);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}

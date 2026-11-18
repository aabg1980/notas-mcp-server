package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;

/**
 * Value Object inmutable (Java record) que representa la retroalimentación
 * (feedback) registrada junto a la calificación de un estudiante.
 *
 * <p>Invariante protegido: la retroalimentación, cuando existe, no excede
 * {@value #MAX_LENGTH} caracteres y siempre está normalizada (sin espacios
 * en los extremos). Puede estar vacía, pero nunca en un estado inválido.</p>
 */
public record Feedback(String value) {

    /** Longitud máxima permitida para la retroalimentación. */
    public static final int MAX_LENGTH = 1000;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo Feedback existente sea válido por definición.
     */
    public Feedback {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new BusinessRuleException(String.format(
                    "La retroalimentación no puede exceder %d caracteres", MAX_LENGTH));
        }
        value = normalized;
    }

    /** Factory method a partir de una representación textual (admite null). */
    public static Feedback of(String value) {
        return new Feedback(value);
    }

    /** Indica si la retroalimentación está vacía. */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }
}

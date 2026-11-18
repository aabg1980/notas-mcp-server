package com.aabg.notas.shared.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;

/**
 * Value Object inmutable (Java record) que representa una descripción opcional
 * de una entidad del dominio académico (rubro, evaluación, criterio o rúbrica).
 *
 * <p>Invariante protegido: la descripción, cuando existe, no excede
 * {@value #MAX_LENGTH} caracteres y siempre está normalizada (sin espacios
 * en los extremos). Puede estar vacía, pero nunca en un estado inválido.</p>
 */
public record Description(String value) {

    /** Longitud máxima permitida para una descripción. */
    public static final int MAX_LENGTH = 500;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo Description existente sea válido por definición.
     */
    public Description {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new BusinessRuleException(String.format(
                    "La descripción no puede exceder %d caracteres", MAX_LENGTH));
        }
        value = normalized;
    }

    /** Factory method a partir de una representación textual (admite null). */
    public static Description of(String value) {
        return new Description(value);
    }

    /** Indica si la descripción está vacía. */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }
}

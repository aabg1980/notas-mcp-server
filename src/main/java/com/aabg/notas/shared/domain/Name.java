package com.aabg.notas.shared.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;

/**
 * Value Object inmutable (Java record) que representa un nombre obligatorio de una
 * entidad del dominio académico (asignatura, rubro, evaluación o criterio de rúbrica).
 *
 * <p>Invariante protegido: el nombre nunca es nulo, ni vacío, ni excede
 * {@value #MAX_LENGTH} caracteres, y siempre está normalizado (sin espacios
 * en los extremos).</p>
 */
public record Name(String value) {

    /** Longitud máxima permitida para un nombre. */
    public static final int MAX_LENGTH = 150;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo Name existente sea válido por definición.
     */
    public Name {
        if (value == null || value.trim().isBlank()) {
            throw new BusinessRuleException("El nombre es obligatorio y no puede estar vacío");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new BusinessRuleException(String.format(
                    "El nombre no puede exceder %d caracteres", MAX_LENGTH));
        }
        value = normalized;
    }

    /** Factory method a partir de una representación textual. */
    public static Name of(String value) {
        return new Name(value);
    }

    /** Compara el nombre con un texto candidato sin distinción de mayúsculas. */
    public boolean equalsIgnoreCase(String candidate) {
        return value.equalsIgnoreCase(candidate == null ? "" : candidate.trim());
    }

    @Override
    public String toString() {
        return value;
    }
}

package com.aabg.notas.course.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;

/**
 * Value Object inmutable (Java record) que representa el identificador de grupo
 * de una asignatura dentro de un periodo académico.
 *
 * <p>Invariante protegido: el ID de grupo nunca es vacío, se normaliza a mayúsculas
 * (ej. 'g01' → 'G01') y admite hasta 50 caracteres.</p>
 */
public record GroupId(String value) {

    /** Longitud máxima permitida para el identificador de grupo. */
    public static final int MAX_LENGTH = 50;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo GroupId existente sea válido por definición.
     */
    public GroupId {
        if (value == null || value.trim().isBlank()) {
            throw new BusinessRuleException("El ID de grupo no puede estar vacío (ej. 'G01')");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() > MAX_LENGTH) {
            throw new BusinessRuleException(String.format(
                    "El ID de grupo no puede exceder %d caracteres (ej. 'G01')", MAX_LENGTH));
        }
        value = normalized;
    }

    /** Factory method a partir de una representación textual. */
    public static GroupId of(String value) {
        return new GroupId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

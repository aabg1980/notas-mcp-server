package com.aabg.notas.course.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.util.regex.Pattern;

/**
 * Value Object inmutable (Java record) que representa el código de una asignatura.
 *
 * <p>Invariante protegido: el código nunca es vacío, se normaliza a mayúsculas
 * (ej. 'mat101' → 'MAT101'), admite hasta 50 caracteres y solo contiene letras,
 * números, puntos, guiones y guiones bajos.</p>
 */
public record CourseCode(String value) {

    /** Formato permitido: letras, números, puntos, guiones y guiones bajos (hasta 50 caracteres). */
    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9._-]{1,50}$");

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo CourseCode existente sea válido por definición.
     */
    public CourseCode {
        if (value == null || value.trim().isBlank()) {
            throw new BusinessRuleException("El código de la asignatura no puede estar vacío");
        }
        String normalized = value.trim().toUpperCase();
        if (!FORMAT.matcher(normalized).matches()) {
            throw new BusinessRuleException(
                    "El código de la asignatura solo admite letras, números, puntos, guiones y guiones bajos, "
                            + "hasta 50 caracteres (ej. 'MAT101')");
        }
        value = normalized;
    }

    /** Factory method a partir de una representación textual. */
    public static CourseCode of(String value) {
        return new CourseCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

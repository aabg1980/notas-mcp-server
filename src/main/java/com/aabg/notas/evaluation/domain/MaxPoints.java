package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object inmutable (Java record) que representa el puntaje máximo asignable
 * a un criterio de rúbrica dentro de una evaluación.
 *
 * <p>Invariante protegido: el valor siempre es estrictamente mayor a 0.0, con escala de 2 decimales.
 * Ejemplo: 'Claridad en la solución' tiene un máximo de 5.00 puntos.</p>
 */
public record MaxPoints(BigDecimal value) {

    /** Límite inferior exclusivo del puntaje máximo. */
    public static final BigDecimal MIN = BigDecimal.ZERO;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo MaxPoints existente sea válido por definición.
     */
    public MaxPoints {
        Objects.requireNonNull(value, "El puntaje máximo del criterio es obligatorio");
        if (value.compareTo(MIN) <= 0) {
            throw new BusinessRuleException(String.format(
                    "El puntaje máximo del criterio debe ser mayor a 0 (recibido: %s)", value));
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Factory method a partir de un valor numérico. */
    public static MaxPoints of(BigDecimal value) {
        return new MaxPoints(value);
    }

    /** Factory method a partir de una representación textual (ej. "5.0"). */
    public static MaxPoints of(String value) {
        return new MaxPoints(new BigDecimal(value));
    }

    @Override
    public String toString() {
        return value.toPlainString() + " pts";
    }
}

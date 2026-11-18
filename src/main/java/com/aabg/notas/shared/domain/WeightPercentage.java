package com.aabg.notas.shared.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object inmutable (Java record) que representa un peso porcentual del dominio académico.
 * Se utiliza para los pesos de rubros, evaluaciones dentro de un rubro y criterios de rúbrica.
 *
 * <p>Invariante protegido: el valor siempre está en el rango (0.0, 100.0], con escala de 2 decimales.
 * Un {@code WeightPercentage} nunca puede existir en estado inválido (modelo siempre válido).</p>
 *
 * <p>Ejemplos: peso de un rubro 'Quices' = 20.00%, peso de 'Quiz 1' dentro de 'Quices' = 50.00%.</p>
 */
public record WeightPercentage(BigDecimal value) {

    /** Límite superior inclusive del peso porcentual. */
    public static final BigDecimal MAX = new BigDecimal("100.00");

    /** Límite inferior exclusivo del peso porcentual. */
    public static final BigDecimal MIN = BigDecimal.ZERO;

    /**
     * Constructor compacto del record: valida y normaliza el valor en la creación.
     * Garantiza que todo WeightPercentage existente sea válido por definición.
     */
    public WeightPercentage {
        Objects.requireNonNull(value, "El peso porcentual es obligatorio");
        if (value.compareTo(MIN) <= 0 || value.compareTo(MAX) > 0) {
            throw new BusinessRuleException(String.format(
                    "El peso porcentual debe estar entre 0.01%% y 100.00%% (recibido: %s)", value));
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Factory method a partir de un valor numérico. */
    public static WeightPercentage of(BigDecimal value) {
        return new WeightPercentage(value);
    }

    /** Factory method a partir de una representación textual (ej. "35.5"). */
    public static WeightPercentage of(String value) {
        return new WeightPercentage(new BigDecimal(value));
    }

    /** Suma inmutable de dos pesos porcentuales (no valida el límite de 100%, eso es responsabilidad del agregado). */
    public WeightPercentage add(WeightPercentage other) {
        Objects.requireNonNull(other, "El peso porcentual a sumar es obligatorio");
        return new WeightPercentage(value.add(other.value));
    }

    /** Indica si la suma de este peso con {@code other} excedería el 100%. */
    public boolean addedWouldExceedMax(WeightPercentage other) {
        return value.add(other.value).compareTo(MAX) > 0;
    }

    /** Indica si la suma de este peso con el valor dado excedería el 100%. */
    public boolean addedWouldExceedMax(BigDecimal rawValue) {
        return value.add(rawValue).compareTo(MAX) > 0;
    }

    @Override
    public String toString() {
        return value.toPlainString() + "%";
    }
}

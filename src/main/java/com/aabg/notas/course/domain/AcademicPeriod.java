package com.aabg.notas.course.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Enum del conjunto finito de periodos académicos válidos para las asignaturas.
 *
 * <p>Cada constante representa un periodo específico (año y corte académico) con su
 * representación canónica tal como se persiste en SQLite (ej. '2027-1'). Cuando el
 * negocio habilite un nuevo periodo, basta con agregar una constante al final.</p>
 *
 * <p>La validación centralizada de {@link #from(String)} garantiza que ningún periodo
 * fuera del catálogo pueda existir en el dominio (always-valid domain model).</p>
 */
public enum AcademicPeriod {

    /** Primer corte académico de 2027. */
    TERM_2027_1("2027-1"),

    /** Tercer corte académico de 2027. */
    TERM_2027_3("2027-3");

    /** Representación canónica del periodo, persistida en la base de datos. */
    private final String value;

    AcademicPeriod(String value) {
        this.value = value;
    }

    /** Valor canónico del periodo tal como se persiste (ej. '2027-1'). */
    public String value() {
        return value;
    }

    /** Año académico del periodo (ej. '2027'). */
    public String year() {
        return value.substring(0, value.indexOf('-'));
    }

    /** Corte académico del periodo (ej. '1'). */
    public String term() {
        return value.substring(value.indexOf('-') + 1);
    }

    /**
     * Resuelve el periodo a partir de su representación textual, tolerando espacios
     * en los extremos y mayúsculas/minúsculas. Lanza BusinessRuleException si el
     * valor no pertenece al catálogo finito de periodos.
     */
    public static AcademicPeriod from(String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        return Arrays.stream(values())
                .filter(period -> period.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(String.format(
                        "El periodo académico '%s' no es válido. Valores permitidos: %s",
                        normalized, allowedValues())));
    }

    private static String allowedValues() {
        return Arrays.stream(values())
                .map(AcademicPeriod::value)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return value;
    }
}

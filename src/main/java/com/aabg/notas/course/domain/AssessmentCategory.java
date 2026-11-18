package com.aabg.notas.course.domain;

import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Entidad interna del agregado Course que representa un rubro de evaluación
 * dentro de una asignatura.
 *
 * <p>Ejemplo: 'Quices' (20%), 'Talleres' (20%), 'Parcial 1' (30%), 'Parcial Final' (30%).
 * Su peso es un {@link WeightPercentage} y por tanto siempre es válido por construcción.</p>
 *
 * <p>Esta entidad no debe instanciarse ni mutarse fuera del agregado raíz {@link Course};
 * todo cambio en su estado es coordinado por la raíz para proteger las invariantes del agregado.</p>
 */
public class AssessmentCategory {

    private final Long id;
    private String name;
    private String description;
    private WeightPercentage weight;

    private AssessmentCategory(Long id, String name, String description, WeightPercentage weight) {
        validateName(name);
        validateDescription(description);

        this.id = id;
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.weight = Objects.requireNonNull(weight, "El peso del rubro es obligatorio");
    }

    /** Factory method para crear un rubro nuevo (aún sin identidad persistida). */
    public static AssessmentCategory create(String name, String description, WeightPercentage weight) {
        return new AssessmentCategory(null, name, description, weight);
    }

    /** Factory method para reconstruir un rubro desde la persistencia. */
    public static AssessmentCategory rehydrate(Long id, String name, String description, WeightPercentage weight) {
        return new AssessmentCategory(id, name, description, weight);
    }

    /** Actualiza el peso del rubro. La raíz del agregado valida la sumatoria total antes de invocar. */
    void updateWeight(WeightPercentage newWeight) {
        Objects.requireNonNull(newWeight, "El nuevo peso del rubro es obligatorio");
        this.weight = newWeight;
    }

    /** Renombra el rubro manteniendo su identidad. */
    void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WeightPercentage getWeight() {
        return weight;
    }

    boolean hasName(String candidateName) {
        return name.equalsIgnoreCase(candidateName == null ? "" : candidateName.trim());
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del rubro no puede estar vacío");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new BusinessRuleException("La descripción del rubro no puede exceder 500 caracteres");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssessmentCategory that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

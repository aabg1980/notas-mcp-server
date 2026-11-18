package com.aabg.notas.course.domain;

import com.aabg.notas.shared.domain.Description;
import com.aabg.notas.shared.domain.Name;
import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Entidad interna del agregado Course que representa un rubro de evaluación
 * dentro de una asignatura.
 *
 * <p>Ejemplo: 'Quices' (20%), 'Talleres' (20%), 'Parcial 1' (30%), 'Parcial Final' (30%).
 * Sus atributos con reglas son Value Objects inmutables ({@link Name},
 * {@link Description}, {@link WeightPercentage}) y por tanto siempre válidos por
 * construcción.</p>
 *
 * <p>Esta entidad no debe instanciarse ni mutarse fuera del agregado raíz {@link Course};
 * todo cambio en su estado es coordinado por la raíz para proteger las invariantes del agregado.</p>
 */
public class AssessmentCategory {

    private final Long id;
    private Name name;
    private Description description;
    private WeightPercentage weight;

    private AssessmentCategory(Long id, String name, String description, WeightPercentage weight) {
        this.id = id;
        this.name = Name.of(name);
        this.description = Description.of(description);
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
        this.name = Name.of(newName);
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }

    public WeightPercentage getWeight() {
        return weight;
    }

    boolean hasName(String candidateName) {
        return name.equalsIgnoreCase(candidateName);
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

package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.domain.Description;
import com.aabg.notas.shared.domain.Name;
import com.aabg.notas.shared.domain.WeightPercentage;
import java.util.Objects;

/**
 * Entidad interna del agregado Evaluation que representa un criterio de la rúbrica
 * de una prueba académica.
 *
 * <p>Ejemplo: 'Claridad en la solución' (máximo 5.00 puntos, peso 40%).
 * Sus atributos con reglas son Value Objects inmutables ({@link Name},
 * {@link Description}, {@link MaxPoints}, {@link WeightPercentage}) y por tanto
 * siempre válidos por construcción.</p>
 *
 * <p>Esta entidad no debe instanciarse ni mutarse fuera del agregado raíz {@link Evaluation};
 * la raíz coordina la sumatoria de pesos de la rúbrica antes de crear nuevos criterios.</p>
 */
public class RubricCriterion {

    private final Long id;
    private Name name;
    private final Description description;
    private final MaxPoints maxPoints;
    private WeightPercentage weight;

    private RubricCriterion(Long id, String name, String description, MaxPoints maxPoints, WeightPercentage weight) {
        this.id = id;
        this.name = Name.of(name);
        this.description = Description.of(description);
        this.maxPoints = Objects.requireNonNull(maxPoints, "El puntaje máximo del criterio es obligatorio");
        this.weight = Objects.requireNonNull(weight, "El peso del criterio es obligatorio");
    }

    /** Factory method para crear un criterio nuevo (aún sin identidad persistida). */
    public static RubricCriterion create(String name, String description, MaxPoints maxPoints, WeightPercentage weight) {
        return new RubricCriterion(null, name, description, maxPoints, weight);
    }

    /** Factory method para reconstruir un criterio desde la persistencia. */
    public static RubricCriterion rehydrate(Long id, String name, String description,
                                            MaxPoints maxPoints, WeightPercentage weight) {
        return new RubricCriterion(id, name, description, maxPoints, weight);
    }

    /** Renombra el criterio manteniendo su identidad. */
    void rename(String newName) {
        this.name = Name.of(newName);
    }

    /** Actualiza el peso del criterio. La raíz del agregado valida la sumatoria antes de invocar. */
    void updateWeight(WeightPercentage newWeight) {
        Objects.requireNonNull(newWeight, "El nuevo peso del criterio es obligatorio");
        this.weight = newWeight;
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

    public MaxPoints getMaxPoints() {
        return maxPoints;
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
        if (!(o instanceof RubricCriterion that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Criterio específico dentro de una rúbrica de evaluación.
 * Ejemplo: 'Claridad en la solución' (Puntos máx: 5.0, Peso: 40%).
 */
public class RubricCriterion {

    private final Long id;
    private final Long evaluationId;
    private final String name;
    private final String description;
    private final double maxPoints;
    private final double weightPercentage;

    public RubricCriterion(Long id, Long evaluationId, String name, String description, double maxPoints, double weightPercentage) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del criterio de rúbrica no puede estar vacío");
        }
        if (maxPoints <= 0.0) {
            throw new BusinessRuleException("Los puntos máximos del criterio deben ser mayores a 0");
        }
        if (weightPercentage <= 0.0 || weightPercentage > 100.0) {
            throw new BusinessRuleException("El porcentaje de peso del criterio debe estar entre 0.1% y 100.0%");
        }

        this.id = id;
        this.evaluationId = evaluationId;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.maxPoints = maxPoints;
        this.weightPercentage = weightPercentage;
    }

    public static RubricCriterion createNew(Long evaluationId, String name, String description, double maxPoints, double weightPercentage) {
        return new RubricCriterion(null, evaluationId, name, description, maxPoints, weightPercentage);
    }

    public Long getId() {
        return id;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    public double getWeightPercentage() {
        return weightPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RubricCriterion that)) return false;
        return Objects.equals(evaluationId, that.evaluationId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationId, name);
    }
}

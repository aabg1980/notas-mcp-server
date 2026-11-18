package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Entidad de dominio que representa un Rubro de Evaluación dentro de una asignatura.
 * Ejemplo: 'Quices' (20%), 'Talleres' (20%), 'Parcial 1' (30%), 'Parcial Final' (30%).
 */
public class AssessmentCategory {

    private final Long id;
    private final Long courseId;
    private final String name;
    private final String description;
    private final double weightPercentage;

    public AssessmentCategory(Long id, Long courseId, String name, String description, double weightPercentage) {
        if (courseId == null) {
            throw new BusinessRuleException("El rubro debe estar asociado a una asignatura");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del rubro no puede estar vacío");
        }
        if (weightPercentage <= 0.0 || weightPercentage > 100.0) {
            throw new BusinessRuleException(String.format("El porcentaje del rubro debe estar entre 0.1%% y 100.0%% (recibido: %.2f%%)", weightPercentage));
        }

        this.id = id;
        this.courseId = courseId;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.weightPercentage = weightPercentage;
    }

    public static AssessmentCategory createNew(Long courseId, String name, String description, double weightPercentage) {
        return new AssessmentCategory(null, courseId, name, description, weightPercentage);
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getWeightPercentage() {
        return weightPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssessmentCategory that)) return false;
        return Objects.equals(courseId, that.courseId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, name);
    }
}

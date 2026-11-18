package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad de dominio que representa una prueba o evaluación específica dentro de un rubro.
 * Puede ser de carácter individual o grupal, y cuenta con rúbrica de evaluación.
 */
public class Evaluation {

    private final Long id;
    private final Long categoryId;
    private final String name;
    private final String description;
    private final EvaluationType evaluationType;
    private final double weightWithinCategory;
    private final LocalDate dueDate;
    private final String rubricDescription;
    private final List<RubricCriterion> criteria;

    public Evaluation(Long id, Long categoryId, String name, String description,
                      EvaluationType evaluationType, double weightWithinCategory,
                      LocalDate dueDate, String rubricDescription, List<RubricCriterion> criteria) {
        if (categoryId == null) {
            throw new BusinessRuleException("La evaluación debe pertenecer a un rubro de evaluación");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre de la evaluación no puede estar vacío");
        }
        if (weightWithinCategory <= 0.0 || weightWithinCategory > 100.0) {
            throw new BusinessRuleException("El peso dentro del rubro debe estar entre 0.1% y 100.0%");
        }

        this.id = id;
        this.categoryId = categoryId;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.evaluationType = evaluationType != null ? evaluationType : EvaluationType.INDIVIDUAL;
        this.weightWithinCategory = weightWithinCategory;
        this.dueDate = dueDate;
        this.rubricDescription = rubricDescription != null ? rubricDescription.trim() : "";
        this.criteria = criteria != null ? List.copyOf(criteria) : Collections.emptyList();
    }

    public static Evaluation createNew(Long categoryId, String name, String description,
                                     EvaluationType type, double weightWithinCategory,
                                     LocalDate dueDate, String rubricDescription) {
        return new Evaluation(null, categoryId, name, description, type, weightWithinCategory, dueDate, rubricDescription, Collections.emptyList());
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EvaluationType getEvaluationType() {
        return evaluationType;
    }

    public double getWeightWithinCategory() {
        return weightWithinCategory;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getRubricDescription() {
        return rubricDescription;
    }

    public List<RubricCriterion> getCriteria() {
        return criteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evaluation that)) return false;
        return Objects.equals(categoryId, that.categoryId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, name);
    }
}

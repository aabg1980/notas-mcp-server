package com.aabg.notas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(
    name = "evaluations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_category_evaluation", columnNames = {"category_id", "name"})
    }
)
public class EvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "evaluation_type", nullable = false, length = 20)
    private String evaluationType;

    @Column(name = "weight_within_category", nullable = false)
    private Double weightWithinCategory;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "rubric_description", columnDefinition = "TEXT")
    private String rubricDescription;

    public EvaluationEntity() {
    }

    public EvaluationEntity(Long id, Long categoryId, String name, String description,
                          String evaluationType, Double weightWithinCategory,
                          LocalDate dueDate, String rubricDescription) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.evaluationType = evaluationType;
        this.weightWithinCategory = weightWithinCategory;
        this.dueDate = dueDate;
        this.rubricDescription = rubricDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    public void setEvaluationType(String evaluationType) {
        this.evaluationType = evaluationType;
    }

    public Double getWeightWithinCategory() {
        return weightWithinCategory;
    }

    public void setWeightWithinCategory(Double weightWithinCategory) {
        this.weightWithinCategory = weightWithinCategory;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getRubricDescription() {
        return rubricDescription;
    }

    public void setRubricDescription(String rubricDescription) {
        this.rubricDescription = rubricDescription;
    }
}

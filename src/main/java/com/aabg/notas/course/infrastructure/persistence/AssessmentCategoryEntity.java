package com.aabg.notas.course.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "assessment_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_category", columnNames = {"course_id", "name"})
    }
)
public class AssessmentCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "weight_percentage", nullable = false)
    private Double weightPercentage;

    public AssessmentCategoryEntity() {
    }

    public AssessmentCategoryEntity(Long id, Long courseId, String name, String description,
                                    Double weightPercentage) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.description = description;
        this.weightPercentage = weightPercentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public Double getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(Double weightPercentage) {
        this.weightPercentage = weightPercentage;
    }
}

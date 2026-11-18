package com.aabg.notas.domain.repository;

import com.aabg.notas.domain.model.AssessmentCategory;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio para la gestión de rubros de evaluación.
 */
public interface AssessmentCategoryRepository {

    AssessmentCategory save(AssessmentCategory category);

    Optional<AssessmentCategory> findById(Long id);

    List<AssessmentCategory> findByCourseId(Long courseId);

    Optional<AssessmentCategory> findByCourseIdAndName(Long courseId, String name);

    double getTotalWeightPercentageByCourse(Long courseId);
}

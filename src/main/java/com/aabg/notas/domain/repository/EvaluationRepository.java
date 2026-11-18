package com.aabg.notas.domain.repository;

import com.aabg.notas.domain.model.Evaluation;
import com.aabg.notas.domain.model.RubricCriterion;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio para la gestión de pruebas, evaluaciones y rúbricas.
 */
public interface EvaluationRepository {

    Evaluation save(Evaluation evaluation);

    Optional<Evaluation> findById(Long id);

    List<Evaluation> findByCategoryId(Long categoryId);

    List<Evaluation> findByCourseId(Long courseId);

    Optional<Evaluation> findByCategoryIdAndName(Long categoryId, String name);

    RubricCriterion saveCriterion(RubricCriterion criterion);

    List<RubricCriterion> findCriteriaByEvaluationId(Long evaluationId);
}

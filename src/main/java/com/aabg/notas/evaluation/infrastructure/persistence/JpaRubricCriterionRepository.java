package com.aabg.notas.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaRubricCriterionRepository extends JpaRepository<RubricCriterionEntity, Long> {

    List<RubricCriterionEntity> findByEvaluationId(Long evaluationId);

    /** Elimina los criterios de la evaluación cuyos ids no estén en la lista dada (criterios removidos del agregado). */
    void deleteByEvaluationIdAndIdNotIn(Long evaluationId, List<Long> ids);

    /** Elimina todos los criterios de una evaluación. */
    void deleteByEvaluationId(Long evaluationId);
}

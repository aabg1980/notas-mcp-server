package com.aabg.notas.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaGradeRepository extends JpaRepository<GradeEntity, Long> {

    List<GradeEntity> findByEvaluationId(Long evaluationId);

    List<GradeEntity> findByStudentId(Long studentId);

    /** Elimina las notas de la evaluación cuyos ids no estén en la lista dada (notas removidas del agregado). */
    void deleteByEvaluationIdAndIdNotIn(Long evaluationId, List<Long> ids);

    /** Elimina todas las notas de una evaluación. */
    void deleteByEvaluationId(Long evaluationId);
}

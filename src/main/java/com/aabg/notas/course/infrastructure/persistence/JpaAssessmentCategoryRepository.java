package com.aabg.notas.course.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaAssessmentCategoryRepository extends JpaRepository<AssessmentCategoryEntity, Long> {

    List<AssessmentCategoryEntity> findByCourseId(Long courseId);

    /** Elimina los rubros de la asignatura cuyos ids no estén en la lista dada (rubros removidos del agregado). */
    void deleteByCourseIdAndIdNotIn(Long courseId, List<Long> ids);

    /** Elimina todos los rubros de una asignatura. */
    void deleteByCourseId(Long courseId);
}

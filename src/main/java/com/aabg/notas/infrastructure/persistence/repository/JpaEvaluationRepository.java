package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaEvaluationRepository extends JpaRepository<EvaluationEntity, Long> {

    List<EvaluationEntity> findByCategoryId(Long categoryId);

    Optional<EvaluationEntity> findByCategoryIdAndName(Long categoryId, String name);

    @Query("""
        SELECT e FROM EvaluationEntity e
        JOIN AssessmentCategoryEntity c ON c.id = e.categoryId
        WHERE c.courseId = :courseId
        ORDER BY c.name, e.name
    """)
    List<EvaluationEntity> findEvaluationsByCourseId(@Param("courseId") Long courseId);
}

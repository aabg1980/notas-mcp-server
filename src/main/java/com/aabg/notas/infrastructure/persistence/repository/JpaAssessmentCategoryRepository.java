package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.AssessmentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaAssessmentCategoryRepository extends JpaRepository<AssessmentCategoryEntity, Long> {

    List<AssessmentCategoryEntity> findByCourseId(Long courseId);

    Optional<AssessmentCategoryEntity> findByCourseIdAndName(Long courseId, String name);

    @Query("SELECT COALESCE(SUM(c.weightPercentage), 0.0) FROM AssessmentCategoryEntity c WHERE c.courseId = :courseId")
    Double sumWeightPercentageByCourseId(@Param("courseId") Long courseId);
}

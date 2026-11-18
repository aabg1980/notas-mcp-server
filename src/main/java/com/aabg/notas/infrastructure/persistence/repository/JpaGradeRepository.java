package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaGradeRepository extends JpaRepository<GradeEntity, Long> {

    Optional<GradeEntity> findByStudentIdAndEvaluationId(Long studentId, Long evaluationId);

    List<GradeEntity> findByStudentId(Long studentId);

    List<GradeEntity> findByEvaluationId(Long evaluationId);

    @Query("""
        SELECT g FROM GradeEntity g
        JOIN EvaluationEntity e ON e.id = g.evaluationId
        JOIN AssessmentCategoryEntity c ON c.id = e.categoryId
        WHERE g.studentId = :studentId AND c.courseId = :courseId
        ORDER BY c.name, e.name
    """)
    List<GradeEntity> findGradesByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("""
        SELECT g FROM GradeEntity g
        JOIN EvaluationEntity e ON e.id = g.evaluationId
        JOIN AssessmentCategoryEntity c ON c.id = e.categoryId
        WHERE c.courseId = :courseId
    """)
    List<GradeEntity> findGradesByCourseId(@Param("courseId") Long courseId);
}

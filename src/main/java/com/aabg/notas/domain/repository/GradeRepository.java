package com.aabg.notas.domain.repository;

import com.aabg.notas.domain.model.Grade;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio para el registro y consulta de calificaciones.
 */
public interface GradeRepository {

    Grade save(Grade grade);

    Optional<Grade> findById(Long id);

    Optional<Grade> findByStudentAndEvaluation(Long studentId, Long evaluationId);

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByEvaluationId(Long evaluationId);

    List<Grade> findByStudentAndCourse(Long studentId, Long courseId);

    List<Grade> findByCourseId(Long courseId);
}

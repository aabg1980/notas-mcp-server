package com.aabg.notas.infrastructure.persistence.adapter;

import com.aabg.notas.domain.model.Grade;
import com.aabg.notas.domain.repository.GradeRepository;
import com.aabg.notas.infrastructure.persistence.entity.GradeEntity;
import com.aabg.notas.infrastructure.persistence.repository.JpaGradeRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class GradeRepositoryAdapter implements GradeRepository {

    private final JpaGradeRepository jpaGradeRepository;

    public GradeRepositoryAdapter(JpaGradeRepository jpaGradeRepository) {
        this.jpaGradeRepository = jpaGradeRepository;
    }

    @Override
    public Grade save(Grade grade) {
        GradeEntity entity = toEntity(grade);
        GradeEntity saved = jpaGradeRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Grade> findById(Long id) {
        return jpaGradeRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Grade> findByStudentAndEvaluation(Long studentId, Long evaluationId) {
        return jpaGradeRepository.findByStudentIdAndEvaluationId(studentId, evaluationId).map(this::toDomain);
    }

    @Override
    public List<Grade> findByStudentId(Long studentId) {
        return jpaGradeRepository.findByStudentId(studentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Grade> findByEvaluationId(Long evaluationId) {
        return jpaGradeRepository.findByEvaluationId(evaluationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Grade> findByStudentAndCourse(Long studentId, Long courseId) {
        return jpaGradeRepository.findGradesByStudentAndCourse(studentId, courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Grade> findByCourseId(Long courseId) {
        return jpaGradeRepository.findGradesByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    private GradeEntity toEntity(Grade domain) {
        return new GradeEntity(
                domain.getId(),
                domain.getStudentId(),
                domain.getEvaluationId(),
                domain.getScore(),
                domain.getFeedback()
        );
    }

    private Grade toDomain(GradeEntity entity) {
        return new Grade(
                entity.getId(),
                entity.getStudentId(),
                entity.getEvaluationId(),
                entity.getScore(),
                entity.getFeedback(),
                entity.getGradedAt()
        );
    }
}

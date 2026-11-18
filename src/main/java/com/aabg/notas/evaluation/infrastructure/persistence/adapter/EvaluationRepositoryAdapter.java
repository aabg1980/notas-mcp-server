package com.aabg.notas.evaluation.infrastructure.persistence.adapter;

import com.aabg.notas.course.infrastructure.persistence.AssessmentCategoryEntity;
import com.aabg.notas.course.infrastructure.persistence.JpaAssessmentCategoryRepository;
import com.aabg.notas.evaluation.domain.Evaluation;
import com.aabg.notas.evaluation.domain.EvaluationRepository;
import com.aabg.notas.evaluation.domain.EvaluationType;
import com.aabg.notas.evaluation.domain.Grade;
import com.aabg.notas.evaluation.domain.GradeScore;
import com.aabg.notas.evaluation.domain.MaxPoints;
import com.aabg.notas.evaluation.domain.RubricCriterion;
import com.aabg.notas.evaluation.infrastructure.persistence.EvaluationEntity;
import com.aabg.notas.evaluation.infrastructure.persistence.GradeEntity;
import com.aabg.notas.evaluation.infrastructure.persistence.JpaEvaluationRepository;
import com.aabg.notas.evaluation.infrastructure.persistence.JpaGradeRepository;
import com.aabg.notas.evaluation.infrastructure.persistence.JpaRubricCriterionRepository;
import com.aabg.notas.evaluation.infrastructure.persistence.RubricCriterionEntity;
import com.aabg.notas.shared.domain.WeightPercentage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia (SQLite + Spring Data JPA) del puerto EvaluationRepository.
 * Persiste el agregado completo: la raíz Evaluation, sus criterios de rúbrica y sus notas,
 * sincronizando las entidades internas que fueron agregadas o removidas dentro del agregado.
 */
@Component
public class EvaluationRepositoryAdapter implements EvaluationRepository {

    private final JpaEvaluationRepository jpaEvaluationRepository;
    private final JpaRubricCriterionRepository jpaRubricCriterionRepository;
    private final JpaGradeRepository jpaGradeRepository;
    private final JpaAssessmentCategoryRepository jpaAssessmentCategoryRepository;

    public EvaluationRepositoryAdapter(JpaEvaluationRepository jpaEvaluationRepository,
                                       JpaRubricCriterionRepository jpaRubricCriterionRepository,
                                       JpaGradeRepository jpaGradeRepository,
                                       JpaAssessmentCategoryRepository jpaAssessmentCategoryRepository) {
        this.jpaEvaluationRepository = jpaEvaluationRepository;
        this.jpaRubricCriterionRepository = jpaRubricCriterionRepository;
        this.jpaGradeRepository = jpaGradeRepository;
        this.jpaAssessmentCategoryRepository = jpaAssessmentCategoryRepository;
    }

    @Override
    @Transactional
    public Evaluation save(Evaluation evaluation) {
        EvaluationEntity saved = jpaEvaluationRepository.save(toEntity(evaluation));
        syncCriteria(evaluation, saved.getId());
        syncGrades(evaluation, saved.getId());
        return findById(saved.getId()).orElseThrow();
    }

    @Override
    public Optional<Evaluation> findById(Long id) {
        return jpaEvaluationRepository.findById(id).map(this::toDomainWithChildren);
    }

    @Override
    public List<Evaluation> findByCategoryId(Long categoryId) {
        return jpaEvaluationRepository.findByCategoryId(categoryId).stream()
                .map(this::toDomainWithChildren)
                .toList();
    }

    @Override
    public List<Evaluation> findByCourseId(Long courseId) {
        List<Long> categoryIds = jpaAssessmentCategoryRepository.findByCourseId(courseId).stream()
                .map(AssessmentCategoryEntity::getId)
                .toList();
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        return jpaEvaluationRepository.findByCategoryIdIn(categoryIds).stream()
                .map(this::toDomainWithChildren)
                .toList();
    }

    @Override
    public List<Evaluation> findAll() {
        return jpaEvaluationRepository.findAll().stream()
                .map(this::toDomainWithChildren)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaGradeRepository.deleteByEvaluationId(id);
        jpaRubricCriterionRepository.deleteByEvaluationId(id);
        jpaEvaluationRepository.deleteById(id);
    }

    // ==================== Sincronización del agregado ====================

    /**
     * Sincroniza los criterios de la rúbrica del agregado con la base de datos:
     * guarda los presentes, con el evaluationId real de la raíz, y elimina los removidos.
     */
    private void syncCriteria(Evaluation evaluation, Long evaluationId) {
        List<Long> keptIds = new ArrayList<>();
        for (RubricCriterion criterion : evaluation.getCriteria()) {
            RubricCriterionEntity entity = new RubricCriterionEntity(
                    criterion.getId(),
                    evaluationId,
                    criterion.getName(),
                    criterion.getDescription(),
                    criterion.getMaxPoints().value().doubleValue(),
                    criterion.getWeight().value().doubleValue());
            RubricCriterionEntity saved = jpaRubricCriterionRepository.save(entity);
            keptIds.add(saved.getId());
        }

        if (keptIds.isEmpty()) {
            jpaRubricCriterionRepository.deleteByEvaluationId(evaluationId);
        } else {
            jpaRubricCriterionRepository.deleteByEvaluationIdAndIdNotIn(evaluationId, keptIds);
        }
    }

    /**
     * Sincroniza las notas del agregado con la base de datos: guarda las presentes,
     * con el evaluationId real de la raíz, y elimina las removidas.
     */
    private void syncGrades(Evaluation evaluation, Long evaluationId) {
        List<Long> keptIds = new ArrayList<>();
        for (Grade grade : evaluation.getGrades()) {
            GradeEntity entity = new GradeEntity(
                    grade.getId(),
                    grade.getStudentId(),
                    evaluationId,
                    grade.getScore().value().doubleValue(),
                    grade.getFeedback());
            GradeEntity saved = jpaGradeRepository.save(entity);
            keptIds.add(saved.getId());
        }

        if (keptIds.isEmpty()) {
            jpaGradeRepository.deleteByEvaluationId(evaluationId);
        } else {
            jpaGradeRepository.deleteByEvaluationIdAndIdNotIn(evaluationId, keptIds);
        }
    }

    // ==================== Mapeo dominio <-> persistencia ====================

    private EvaluationEntity toEntity(Evaluation domain) {
        return new EvaluationEntity(
                domain.getId(),
                domain.getCategoryId(),
                domain.getName(),
                domain.getDescription(),
                domain.getType().name(),
                domain.getWeightWithinCategory().value().doubleValue(),
                domain.getDueDate(),
                domain.getRubricDescription());
    }

    private Evaluation toDomainWithChildren(EvaluationEntity entity) {
        List<RubricCriterion> criteria = jpaRubricCriterionRepository.findByEvaluationId(entity.getId()).stream()
                .map(this::toCriterionDomain)
                .toList();

        List<Grade> grades = jpaGradeRepository.findByEvaluationId(entity.getId()).stream()
                .map(this::toGradeDomain)
                .toList();

        return Evaluation.rehydrate(
                entity.getId(),
                entity.getCategoryId(),
                entity.getName(),
                entity.getDescription(),
                EvaluationType.valueOf(entity.getEvaluationType()),
                WeightPercentage.of(BigDecimal.valueOf(entity.getWeightWithinCategory())),
                entity.getDueDate(),
                entity.getRubricDescription(),
                criteria,
                grades);
    }

    private RubricCriterion toCriterionDomain(RubricCriterionEntity entity) {
        return RubricCriterion.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                MaxPoints.of(BigDecimal.valueOf(entity.getMaxPoints())),
                WeightPercentage.of(BigDecimal.valueOf(entity.getWeightPercentage())));
    }

    private Grade toGradeDomain(GradeEntity entity) {
        return Grade.rehydrate(
                entity.getId(),
                entity.getStudentId(),
                GradeScore.of(BigDecimal.valueOf(entity.getScore())),
                entity.getFeedback(),
                entity.getGradedAt());
    }
}

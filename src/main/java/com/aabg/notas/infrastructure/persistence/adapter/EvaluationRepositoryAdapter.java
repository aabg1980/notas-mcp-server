package com.aabg.notas.infrastructure.persistence.adapter;

import com.aabg.notas.domain.model.Evaluation;
import com.aabg.notas.domain.model.EvaluationType;
import com.aabg.notas.domain.model.RubricCriterion;
import com.aabg.notas.domain.repository.EvaluationRepository;
import com.aabg.notas.infrastructure.persistence.entity.EvaluationEntity;
import com.aabg.notas.infrastructure.persistence.entity.RubricCriterionEntity;
import com.aabg.notas.infrastructure.persistence.repository.JpaEvaluationRepository;
import com.aabg.notas.infrastructure.persistence.repository.JpaRubricCriterionRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class EvaluationRepositoryAdapter implements EvaluationRepository {

    private final JpaEvaluationRepository jpaEvaluationRepository;
    private final JpaRubricCriterionRepository jpaRubricRepository;

    public EvaluationRepositoryAdapter(JpaEvaluationRepository jpaEvaluationRepository,
                                     JpaRubricCriterionRepository jpaRubricRepository) {
        this.jpaEvaluationRepository = jpaEvaluationRepository;
        this.jpaRubricRepository = jpaRubricRepository;
    }

    @Override
    public Evaluation save(Evaluation evaluation) {
        EvaluationEntity entity = toEntity(evaluation);
        EvaluationEntity saved = jpaEvaluationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Evaluation> findById(Long id) {
        return jpaEvaluationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Evaluation> findByCategoryId(Long categoryId) {
        return jpaEvaluationRepository.findByCategoryId(categoryId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Evaluation> findByCourseId(Long courseId) {
        return jpaEvaluationRepository.findEvaluationsByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Evaluation> findByCategoryIdAndName(Long categoryId, String name) {
        return jpaEvaluationRepository.findByCategoryIdAndName(categoryId, name).map(this::toDomain);
    }

    @Override
    public RubricCriterion saveCriterion(RubricCriterion criterion) {
        RubricCriterionEntity entity = new RubricCriterionEntity(
                criterion.getId(),
                criterion.getEvaluationId(),
                criterion.getName(),
                criterion.getDescription(),
                criterion.getMaxPoints(),
                criterion.getWeightPercentage()
        );
        RubricCriterionEntity saved = jpaRubricRepository.save(entity);
        return toDomainCriterion(saved);
    }

    @Override
    public List<RubricCriterion> findCriteriaByEvaluationId(Long evaluationId) {
        return jpaRubricRepository.findByEvaluationId(evaluationId).stream()
                .map(this::toDomainCriterion)
                .toList();
    }

    private EvaluationEntity toEntity(Evaluation domain) {
        return new EvaluationEntity(
                domain.getId(),
                domain.getCategoryId(),
                domain.getName(),
                domain.getDescription(),
                domain.getEvaluationType().name(),
                domain.getWeightWithinCategory(),
                domain.getDueDate(),
                domain.getRubricDescription()
        );
    }

    private Evaluation toDomain(EvaluationEntity entity) {
        List<RubricCriterion> criteria = findCriteriaByEvaluationId(entity.getId());
        EvaluationType type = EvaluationType.valueOf(entity.getEvaluationType());
        return new Evaluation(
                entity.getId(),
                entity.getCategoryId(),
                entity.getName(),
                entity.getDescription(),
                type,
                entity.getWeightWithinCategory(),
                entity.getDueDate(),
                entity.getRubricDescription(),
                criteria
        );
    }

    private RubricCriterion toDomainCriterion(RubricCriterionEntity entity) {
        return new RubricCriterion(
                entity.getId(),
                entity.getEvaluationId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMaxPoints(),
                entity.getWeightPercentage()
        );
    }
}

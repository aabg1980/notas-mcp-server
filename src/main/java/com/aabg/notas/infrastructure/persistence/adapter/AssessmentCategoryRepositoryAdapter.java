package com.aabg.notas.infrastructure.persistence.adapter;

import com.aabg.notas.domain.model.AssessmentCategory;
import com.aabg.notas.domain.repository.AssessmentCategoryRepository;
import com.aabg.notas.infrastructure.persistence.entity.AssessmentCategoryEntity;
import com.aabg.notas.infrastructure.persistence.repository.JpaAssessmentCategoryRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class AssessmentCategoryRepositoryAdapter implements AssessmentCategoryRepository {

    private final JpaAssessmentCategoryRepository jpaCategoryRepository;

    public AssessmentCategoryRepositoryAdapter(JpaAssessmentCategoryRepository jpaCategoryRepository) {
        this.jpaCategoryRepository = jpaCategoryRepository;
    }

    @Override
    public AssessmentCategory save(AssessmentCategory category) {
        AssessmentCategoryEntity entity = toEntity(category);
        AssessmentCategoryEntity saved = jpaCategoryRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AssessmentCategory> findById(Long id) {
        return jpaCategoryRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AssessmentCategory> findByCourseId(Long courseId) {
        return jpaCategoryRepository.findByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<AssessmentCategory> findByCourseIdAndName(Long courseId, String name) {
        return jpaCategoryRepository.findByCourseIdAndName(courseId, name).map(this::toDomain);
    }

    @Override
    public double getTotalWeightPercentageByCourse(Long courseId) {
        Double sum = jpaCategoryRepository.sumWeightPercentageByCourseId(courseId);
        return sum != null ? sum : 0.0;
    }

    private AssessmentCategoryEntity toEntity(AssessmentCategory domain) {
        return new AssessmentCategoryEntity(
                domain.getId(),
                domain.getCourseId(),
                domain.getName(),
                domain.getDescription(),
                domain.getWeightPercentage()
        );
    }

    private AssessmentCategory toDomain(AssessmentCategoryEntity entity) {
        return new AssessmentCategory(
                entity.getId(),
                entity.getCourseId(),
                entity.getName(),
                entity.getDescription(),
                entity.getWeightPercentage()
        );
    }
}

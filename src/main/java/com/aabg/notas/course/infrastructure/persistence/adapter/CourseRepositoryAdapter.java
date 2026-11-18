package com.aabg.notas.course.infrastructure.persistence.adapter;

import com.aabg.notas.course.domain.AssessmentCategory;
import com.aabg.notas.course.domain.Course;
import com.aabg.notas.course.domain.CourseRepository;
import com.aabg.notas.course.domain.CourseStatus;
import com.aabg.notas.course.infrastructure.persistence.AssessmentCategoryEntity;
import com.aabg.notas.course.infrastructure.persistence.CourseEntity;
import com.aabg.notas.course.infrastructure.persistence.JpaAssessmentCategoryRepository;
import com.aabg.notas.course.infrastructure.persistence.JpaCourseRepository;
import com.aabg.notas.shared.domain.WeightPercentage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia (SQLite + Spring Data JPA) del puerto CourseRepository.
 * Persiste el agregado completo: la raíz Course y sus entidades internas AssessmentCategory,
 * sincronizando los rubros que fueron agregados o removidos dentro del agregado.
 */
@Component
public class CourseRepositoryAdapter implements CourseRepository {

    private final JpaCourseRepository jpaCourseRepository;
    private final JpaAssessmentCategoryRepository jpaAssessmentCategoryRepository;

    public CourseRepositoryAdapter(JpaCourseRepository jpaCourseRepository,
                                   JpaAssessmentCategoryRepository jpaAssessmentCategoryRepository) {
        this.jpaCourseRepository = jpaCourseRepository;
        this.jpaAssessmentCategoryRepository = jpaAssessmentCategoryRepository;
    }

    @Override
    @Transactional
    public Course save(Course course) {
        CourseEntity saved = jpaCourseRepository.save(toEntity(course));
        syncCategories(course, saved.getId());
        return findById(saved.getId()).orElseThrow();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaCourseRepository.findById(id).map(this::toDomainWithCategories);
    }

    @Override
    public Optional<Course> findByCodePeriodAndGroup(String code, String academicPeriod, String groupId) {
        return jpaCourseRepository
                .findByCodeAndAcademicPeriodAndGroupId(code.trim().toUpperCase(), academicPeriod.trim(),
                        groupId.trim().toUpperCase())
                .map(this::toDomainWithCategories);
    }

    @Override
    public List<Course> findAll() {
        return jpaCourseRepository.findAll().stream()
                .map(this::toDomainWithCategories)
                .toList();
    }

    @Override
    public List<Course> findByAcademicPeriod(String academicPeriod) {
        return jpaCourseRepository.findByAcademicPeriod(academicPeriod).stream()
                .map(this::toDomainWithCategories)
                .toList();
    }

    @Override
    public List<Course> findByCode(String code) {
        return jpaCourseRepository.findByCode(code.trim().toUpperCase()).stream()
                .map(this::toDomainWithCategories)
                .toList();
    }

    // ==================== Sincronización del agregado ====================

    /**
     * Sincroniza los rubros del agregado con la base de datos: guarda los presentes,
     * con el courseId real de la raíz, y elimina los que fueron removidos del agregado.
     */
    private void syncCategories(Course course, Long courseId) {
        List<Long> keptIds = new ArrayList<>();
        for (AssessmentCategory category : course.getCategories()) {
            AssessmentCategoryEntity entity = new AssessmentCategoryEntity(
                    category.getId(),
                    courseId,
                    category.getName().value(),
                    category.getDescription().value(),
                    category.getWeight().value().doubleValue());
            AssessmentCategoryEntity saved = jpaAssessmentCategoryRepository.save(entity);
            keptIds.add(saved.getId());
        }

        if (keptIds.isEmpty()) {
            jpaAssessmentCategoryRepository.deleteByCourseId(courseId);
        } else {
            jpaAssessmentCategoryRepository.deleteByCourseIdAndIdNotIn(courseId, keptIds);
        }
    }

    // ==================== Mapeo dominio <-> persistencia ====================

    private CourseEntity toEntity(Course domain) {
        return new CourseEntity(
                domain.getId(),
                domain.getCode().value(),
                domain.getName().value(),
                domain.getAcademicPeriod().value(),
                domain.getGroupId().value(),
                domain.getStatus().name());
    }

    private Course toDomainWithCategories(CourseEntity entity) {
        List<AssessmentCategory> categories = jpaAssessmentCategoryRepository.findByCourseId(entity.getId())
                .stream()
                .map(this::toCategoryDomain)
                .toList();

        return Course.rehydrate(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAcademicPeriod(),
                entity.getGroupId(),
                CourseStatus.valueOf(entity.getStatus()),
                categories);
    }

    private AssessmentCategory toCategoryDomain(AssessmentCategoryEntity entity) {
        return AssessmentCategory.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                WeightPercentage.of(BigDecimal.valueOf(entity.getWeightPercentage())));
    }
}

package com.aabg.notas.course.application;

import com.aabg.notas.course.application.dto.AssessmentCategoryDto;
import com.aabg.notas.course.application.dto.CourseDto;
import com.aabg.notas.course.domain.AssessmentCategory;
import com.aabg.notas.course.domain.Course;
import com.aabg.notas.course.domain.CourseRepository;
import com.aabg.notas.evaluation.domain.EvaluationRepository;
import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación (casos de uso) del módulo Course.
 *
 * <p>Con el modelo rico, este servicio solo orquesta: carga el agregado por su
 * puerto, invoca comportamiento de la raíz y persiste. Las invariantes (sumatoria
 * de pesos, rubros únicos, curso cerrado) viven en el agregado.</p>
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EvaluationRepository evaluationRepository;

    public CourseService(CourseRepository courseRepository, EvaluationRepository evaluationRepository) {
        this.courseRepository = courseRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public CourseDto createCourse(String code, String name, String academicPeriod, String groupId) {
        if (courseRepository.findByCodePeriodAndGroup(code, academicPeriod, groupId).isPresent()) {
            throw new BusinessRuleException(String.format(
                    "Ya existe la asignatura '%s' en el periodo '%s' grupo '%s'",
                    code, academicPeriod, groupId));
        }
        Course course = courseRepository.save(Course.create(code, name, academicPeriod, groupId));
        return toDto(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listCourses() {
        return courseRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CourseDto getCourse(String code, String academicPeriod, String groupId) {
        return toDto(requireCourse(code, academicPeriod, groupId));
    }

    @Transactional
    public CourseDto addCategory(String code, String academicPeriod, String groupId, String name,
                                 String description, Double weightPercentage) {
        Course course = requireCourse(code, academicPeriod, groupId);
        course.addCategory(name, description, WeightPercentage.of(BigDecimal.valueOf(weightPercentage)));
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto updateCategoryWeight(String code, String academicPeriod, String groupId, Long categoryId,
                                          Double weightPercentage) {
        Course course = requireCourse(code, academicPeriod, groupId);
        course.updateCategoryWeight(categoryId, WeightPercentage.of(BigDecimal.valueOf(weightPercentage)));
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto removeCategory(String code, String academicPeriod, String groupId, Long categoryId) {
        Course course = requireCourse(code, academicPeriod, groupId);
        ensureCategoryHasNoEvaluations(course, categoryId);
        course.removeCategory(categoryId);
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto closeCourse(String code, String academicPeriod, String groupId) {
        Course course = requireCourse(code, academicPeriod, groupId);
        course.close();
        return toDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto reopenCourse(String code, String academicPeriod, String groupId) {
        Course course = requireCourse(code, academicPeriod, groupId);
        course.reopen();
        return toDto(courseRepository.save(course));
    }

    // ==================== Soporte interno ====================

    private Course requireCourse(String code, String academicPeriod, String groupId) {
        return courseRepository.findByCodePeriodAndGroup(code, academicPeriod, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura",
                        code + "-" + academicPeriod + "-" + groupId));
    }

    /** Regla coordinada entre agregados: no se puede eliminar un rubro con evaluaciones asociadas. */
    private void ensureCategoryHasNoEvaluations(Course course, Long categoryId) {
        AssessmentCategory category = course.findCategoryById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Rubro de evaluación", String.valueOf(categoryId)));

        if (category.getId() != null && !evaluationRepository.findByCategoryId(category.getId()).isEmpty()) {
            throw new BusinessRuleException(String.format(
                    "El rubro '%s' tiene evaluaciones registradas; elimínelas primero",
                    category.getName()));
        }
    }

    private CourseDto toDto(Course course) {
        List<AssessmentCategoryDto> categories = course.getCategories().stream()
                .map(c -> new AssessmentCategoryDto(
                        c.getId(),
                        c.getName().value(),
                        c.getDescription().value(),
                        c.getWeight().value().doubleValue()))
                .toList();

        return new CourseDto(
                course.getId(),
                course.getCode().value(),
                course.getName().value(),
                course.getAcademicPeriod().value(),
                course.getGroupId().value(),
                course.getStatus().name(),
                course.totalWeight().doubleValue(),
                categories);
    }
}

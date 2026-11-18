package com.aabg.notas.evaluation.application;

import com.aabg.notas.course.domain.AssessmentCategory;
import com.aabg.notas.course.domain.Course;
import com.aabg.notas.course.domain.CourseRepository;
import com.aabg.notas.evaluation.application.dto.EvaluationDto;
import com.aabg.notas.evaluation.application.dto.GradeDto;
import com.aabg.notas.evaluation.application.dto.RubricCriterionDto;
import com.aabg.notas.evaluation.domain.Evaluation;
import com.aabg.notas.evaluation.domain.EvaluationRepository;
import com.aabg.notas.evaluation.domain.EvaluationType;
import com.aabg.notas.evaluation.domain.Grade;
import com.aabg.notas.evaluation.domain.GradeScore;
import com.aabg.notas.evaluation.domain.MaxPoints;
import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import com.aabg.notas.student.application.dto.StudentDto;
import com.aabg.notas.student.domain.Student;
import com.aabg.notas.student.domain.StudentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación (casos de uso) del módulo Evaluation.
 *
 * <p>Con el modelo rico, este servicio solo orquesta: carga agregados por sus
 * puertos, invoca comportamiento de las raíces y persiste. Las invariantes del
 * agregado (sumatoria de rúbrica, nota única por estudiante, escala 0.0 a 5.0)
 * viven en la raíz Evaluation y en los Value Objects.</p>
 */
@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EvaluationService(EvaluationRepository evaluationRepository, CourseRepository courseRepository,
                             StudentRepository studentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Crea una evaluación dentro de un rubro de una asignatura.
     * Reglas coordinadas entre agregados: el curso debe estar abierto y la sumatoria
     * de pesos de evaluaciones del rubro no puede exceder el 100%.
     */
    @Transactional
    public EvaluationDto createEvaluation(String courseCode, String academicPeriod, String groupId,
                                          String categoryName, String name, String description, String type,
                                          Double weightWithinCategory, String dueDate, String rubricDescription) {
        Course course = requireCourse(courseCode, academicPeriod, groupId);
        if (!course.isOpen()) {
            throw new BusinessRuleException(String.format(
                    "La asignatura '%s' está cerrada y no admite nuevas evaluaciones",
                    course.getUniqueKey()));
        }

        AssessmentCategory category = findCategoryByName(course, categoryName);
        BigDecimal newWeight = WeightPercentage.of(BigDecimal.valueOf(weightWithinCategory)).value();
        BigDecimal existingWeights = evaluationRepository.findByCategoryId(category.getId()).stream()
                .map(e -> e.getWeightWithinCategory().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (existingWeights.add(newWeight).compareTo(WeightPercentage.MAX) > 0) {
            throw new BusinessRuleException(String.format(
                    "No es posible crear la evaluación '%s' con peso %s: la sumatoria de pesos de evaluaciones "
                            + "del rubro '%s' excedería el 100%% (total sería %s)",
                    name, newWeight, category.getName(),
                    existingWeights.add(newWeight).setScale(2).toPlainString()));
        }

        Evaluation evaluation = evaluationRepository.save(Evaluation.create(
                category.getId(),
                name,
                description,
                parseType(type),
                WeightPercentage.of(newWeight),
                parseDueDate(dueDate),
                rubricDescription));
        return toDto(evaluation, null);
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> listEvaluations(String courseCode, String academicPeriod, String groupId) {
        Course course = requireCourse(courseCode, academicPeriod, groupId);
        return evaluationRepository.findByCourseId(course.getId()).stream()
                .map(e -> toDto(e, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluationDto getEvaluation(Long evaluationId) {
        return toDto(requireEvaluation(evaluationId), null);
    }

    @Transactional
    public EvaluationDto addCriterion(Long evaluationId, String name, String description, Double maxPoints,
                                      Double weightPercentage) {
        Evaluation evaluation = requireEvaluation(evaluationId);
        evaluation.addCriterion(name, description,
                MaxPoints.of(BigDecimal.valueOf(maxPoints)),
                WeightPercentage.of(BigDecimal.valueOf(weightPercentage)));
        return toDto(evaluationRepository.save(evaluation), null);
    }

    @Transactional
    public EvaluationDto removeCriterion(Long evaluationId, Long criterionId) {
        Evaluation evaluation = requireEvaluation(evaluationId);
        evaluation.removeCriterion(criterionId);
        return toDto(evaluationRepository.save(evaluation), null);
    }

    /**
     * Asienta (o actualiza) la nota de un estudiante en una evaluación.
     * El estudiante debe estar activo; la escala 0.0 a 5.0 y la nota única por
     * estudiante las garantiza el agregado y el Value Object GradeScore.
     */
    @Transactional
    public GradeDto recordGrade(String studentCode, Long evaluationId, Double score, String feedback) {
        Student student = studentRepository.findByCode(studentCode)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante", studentCode));
        student.validateCanBeEvaluated();

        Evaluation evaluation = requireEvaluation(evaluationId);
        evaluation.recordGrade(student.getId(), GradeScore.of(BigDecimal.valueOf(score)), feedback);

        Evaluation saved = evaluationRepository.save(evaluation);
        Grade grade = saved.gradeForStudent(student.getId()).orElseThrow();
        return toDto(grade, student);
    }

    @Transactional(readOnly = true)
    public List<GradeDto> listGrades(Long evaluationId) {
        Evaluation evaluation = requireEvaluation(evaluationId);
        return evaluation.getGrades().stream()
                .map(grade -> toDto(grade, studentRepository.findById(grade.getStudentId()).orElse(null)))
                .toList();
    }

    // ==================== Soporte interno ====================

    private Course requireCourse(String courseCode, String academicPeriod, String groupId) {
        return courseRepository.findByCodePeriodAndGroup(courseCode, academicPeriod, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura",
                        courseCode + "-" + academicPeriod + "-" + groupId));
    }

    private Evaluation requireEvaluation(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluación", String.valueOf(evaluationId)));
    }

    private AssessmentCategory findCategoryByName(Course course, String categoryName) {
        return course.getCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Rubro", categoryName));
    }

    private EvaluationType parseType(String type) {
        String normalized = type == null || type.isBlank() ? EvaluationType.INDIVIDUAL.name() : type.trim().toUpperCase();
        try {
            return EvaluationType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(String.format(
                    "El tipo de evaluación '%s' no es válido. Valores permitidos: INDIVIDUAL, GROUP", type));
        }
    }

    private LocalDate parseDueDate(String dueDate) {
        if (dueDate == null || dueDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dueDate);
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException(String.format(
                    "La fecha límite '%s' no es válida. Use el formato AAAA-MM-DD (ej. '2027-03-20')", dueDate));
        }
    }

    private EvaluationDto toDto(Evaluation evaluation, Student knownStudent) {
        List<RubricCriterionDto> criteria = evaluation.getCriteria().stream()
                .map(c -> new RubricCriterionDto(
                        c.getId(),
                        c.getName().value(),
                        c.getDescription().value(),
                        c.getMaxPoints().value().doubleValue(),
                        c.getWeight().value().doubleValue()))
                .toList();

        List<GradeDto> grades = evaluation.getGrades().stream()
                .map(grade -> toDto(grade, resolveStudent(grade, knownStudent)))
                .toList();

        return new EvaluationDto(
                evaluation.getId(),
                evaluation.getCategoryId(),
                evaluation.getName().value(),
                evaluation.getDescription().value(),
                evaluation.getType().name(),
                evaluation.getWeightWithinCategory().value().doubleValue(),
                evaluation.getDueDate(),
                evaluation.getRubricDescription().value(),
                criteria,
                grades);
    }

    private Student resolveStudent(Grade grade, Student knownStudent) {
        if (knownStudent != null && knownStudent.getId().equals(grade.getStudentId())) {
            return knownStudent;
        }
        return studentRepository.findById(grade.getStudentId()).orElse(null);
    }

    private GradeDto toDto(Grade grade, Student student) {
        return new GradeDto(
                grade.getId(),
                grade.getStudentId(),
                student != null ? student.getCode() : null,
                student != null ? student.getFullName() : null,
                grade.getScore().value().doubleValue(),
                grade.getFeedback().value(),
                grade.getGradedAt(),
                grade.isPassing());
    }
}

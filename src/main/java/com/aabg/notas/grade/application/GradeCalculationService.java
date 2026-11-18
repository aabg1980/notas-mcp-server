package com.aabg.notas.grade.application;

import com.aabg.notas.course.domain.Course;
import com.aabg.notas.course.domain.CourseRepository;
import com.aabg.notas.evaluation.domain.Evaluation;
import com.aabg.notas.evaluation.domain.EvaluationRepository;
import com.aabg.notas.grade.application.dto.CourseSummaryDto;
import com.aabg.notas.grade.application.dto.FinalGradeReportDto;
import com.aabg.notas.grade.application.dto.GradeContributionDto;
import com.aabg.notas.grade.application.dto.StudentCourseGradeDto;
import com.aabg.notas.grade.application.dto.StudentFinalGradeRowDto;
import com.aabg.notas.grade.domain.FinalGradeCalculator.FinalGradeResult;
import com.aabg.notas.grade.domain.FinalGradeCalculator;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import com.aabg.notas.student.domain.Student;
import com.aabg.notas.student.domain.StudentRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación de cálculo de notas. Orquesta la carga de los agregados
 * Course, Evaluation y Student y delega el cálculo puro en el servicio de dominio
 * {@link FinalGradeCalculator}.
 */
@Service
public class GradeCalculationService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EvaluationRepository evaluationRepository;

    public GradeCalculationService(CourseRepository courseRepository, StudentRepository studentRepository,
                                   EvaluationRepository evaluationRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.evaluationRepository = evaluationRepository;
    }

    /** Calcula la nota definitiva ponderada de un estudiante en una asignatura. */
    @Transactional(readOnly = true)
    public FinalGradeReportDto calculateFinalGrade(String studentCode, String courseCode, String academicPeriod,
                                                   String groupId) {
        Student student = requireStudent(studentCode);
        Course course = requireCourse(courseCode, academicPeriod, groupId);
        FinalGradeResult result = computeFor(student.getId(), course);

        List<GradeContributionDto> detail = result.contributions().stream()
                .map(c -> new GradeContributionDto(
                        c.evaluationId(),
                        c.categoryName(),
                        c.evaluationName(),
                        c.effectiveWeightPct().doubleValue(),
                        c.score() != null ? c.score().doubleValue() : null,
                        c.contribution().doubleValue(),
                        c.graded()))
                .toList();

        return new FinalGradeReportDto(
                student.getCode(),
                student.getFullName(),
                course.getCode().value(),
                course.getName().value(),
                course.getAcademicPeriod().value(),
                course.getGroupId().value(),
                result.finalGrade(),
                result.accumulatedWeightPct(),
                result.status().name(),
                result.gradedCount(),
                result.pendingCount(),
                detail);
    }

    /** Genera el reporte consolidado de definitivas de todos los estudiantes de una asignatura. */
    @Transactional(readOnly = true)
    public CourseSummaryDto getCourseSummary(String courseCode, String academicPeriod, String groupId) {
        Course course = requireCourse(courseCode, academicPeriod, groupId);
        List<Student> students = studentRepository.findByCourseId(course.getId());
        List<Evaluation> evaluations = evaluationRepository.findByCourseId(course.getId());

        List<StudentFinalGradeRowDto> rows = students.stream()
                .map(student -> new StudentWithResult(student, computeFor(student.getId(), course)))
                .sorted(Comparator.reverseOrder())
                .map(this::toRow)
                .toList();

        long gradedStudents = rows.stream().filter(r -> "APPROVED".equals(r.status()) || "REJECTED".equals(r.status()))
                .count();
        long approvedCount = rows.stream().filter(r -> "APPROVED".equals(r.status())).count();
        long rejectedCount = rows.stream().filter(r -> "REJECTED".equals(r.status())).count();
        long inProgressCount = rows.stream().filter(r -> "IN_PROGRESS".equals(r.status())).count();

        return new CourseSummaryDto(
                course.getCode().value(),
                course.getName().value(),
                course.getAcademicPeriod().value(),
                course.getGroupId().value(),
                students.size(),
                (int) gradedStudents,
                (int) approvedCount,
                (int) rejectedCount,
                (int) inProgressCount,
                rows);
    }

    /** Historial de notas de un estudiante en una asignatura, con el peso de cada evaluación. */
    @Transactional(readOnly = true)
    public List<StudentCourseGradeDto> getStudentGrades(String studentCode, String courseCode, String academicPeriod,
                                                        String groupId) {
        Student student = requireStudent(studentCode);
        Course course = requireCourse(courseCode, academicPeriod, groupId);
        List<Evaluation> evaluations = evaluationRepository.findByCourseId(course.getId());

        FinalGradeResult result = computeFor(student.getId(), course);

        return result.contributions().stream()
                .filter(c -> c.graded())
                .map(contribution -> {
                    Evaluation evaluation = evaluations.stream()
                            .filter(e -> e.getId().equals(contribution.evaluationId()))
                            .findFirst()
                            .orElseThrow();
                    var grade = evaluation.gradeForStudent(student.getId()).orElseThrow();
                    return new StudentCourseGradeDto(
                            evaluation.getId(),
                            contribution.categoryName(),
                            contribution.evaluationName(),
                            contribution.effectiveWeightPct().doubleValue(),
                            grade.getScore().value().doubleValue(),
                            grade.getFeedback().value(),
                            grade.getGradedAt(),
                            grade.isPassing());
                })
                .toList();
    }

    // ==================== Soporte interno ====================

    private FinalGradeResult computeFor(Long studentId, Course course) {
        List<Evaluation> evaluations = evaluationRepository.findByCourseId(course.getId());
        return FinalGradeCalculator.compute(studentId, course, evaluations);
    }

    private Student requireStudent(String studentCode) {
        return studentRepository.findByCode(studentCode)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante", studentCode));
    }

    private Course requireCourse(String courseCode, String academicPeriod, String groupId) {
        return courseRepository.findByCodePeriodAndGroup(courseCode, academicPeriod, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura",
                        courseCode + "-" + academicPeriod + "-" + groupId));
    }

    /** Par ordenable (por definitiva) estudiante + resultado del cálculo. */
    private record StudentWithResult(Student student, FinalGradeResult result)
            implements Comparable<StudentWithResult> {

        @Override
        public int compareTo(StudentWithResult other) {
            return result.finalGrade().compareTo(other.result.finalGrade());
        }
    }

    private StudentFinalGradeRowDto toRow(StudentWithResult entry) {
        return new StudentFinalGradeRowDto(
                entry.student().getCode(),
                entry.student().getFullName(),
                entry.result().finalGrade(),
                entry.result().accumulatedWeightPct(),
                entry.result().status().name());
    }
}

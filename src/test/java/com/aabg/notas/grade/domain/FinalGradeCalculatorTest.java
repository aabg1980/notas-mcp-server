package com.aabg.notas.grade.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.aabg.notas.course.domain.AssessmentCategory;
import com.aabg.notas.course.domain.Course;
import com.aabg.notas.course.domain.CourseStatus;
import com.aabg.notas.evaluation.domain.Evaluation;
import com.aabg.notas.evaluation.domain.EvaluationType;
import com.aabg.notas.evaluation.domain.Grade;
import com.aabg.notas.evaluation.domain.GradeScore;
import com.aabg.notas.grade.domain.FinalGradeCalculator.FinalGradeResult;
import com.aabg.notas.grade.domain.FinalGradeCalculator.FinalGradeStatus;
import com.aabg.notas.shared.domain.WeightPercentage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de dominio puro de cálculo de la definitiva
 * ponderada. El cálculo cruza los agregados Course (estructura ponderada) y
 * Evaluation (notas), por eso es un servicio de dominio.
 */
class FinalGradeCalculatorTest {

    /**
     * Estructura tipo:
     * - Parciales (40%) → Parcial 1 (100% del rubro)
     * - Quices (60%)    → Quiz 1 (50%), Quiz 2 (50%)
     */
    private Course course() {
        return Course.rehydrate(
                1L, "MAT101", "Matemáticas I", "2027-1", "G01", CourseStatus.OPEN,
                List.of(
                        AssessmentCategory.rehydrate(10L, "Parciales", "", WeightPercentage.of("40.00")),
                        AssessmentCategory.rehydrate(11L, "Quices", "", WeightPercentage.of("60.00"))));
    }

    private List<Evaluation> evaluations() {
        return List.of(
                evaluation(100L, 10L, "Parcial 1", "100.00"),
                evaluation(101L, 11L, "Quiz 1", "50.00"),
                evaluation(102L, 11L, "Quiz 2", "50.00"));
    }

    private Evaluation evaluation(Long id, Long categoryId, String name, String weight) {
        return Evaluation.rehydrate(id, categoryId, name, "", EvaluationType.INDIVIDUAL,
                WeightPercentage.of(weight), LocalDate.of(2027, 3, 20), "",
                List.of(), List.of());
    }

    private static Grade grade(Long studentId, String score) {
        return Grade.rehydrate(null, studentId, GradeScore.of(score), "", LocalDate.of(2027, 4, 1).atStartOfDay());
    }

    private List<Evaluation> withGrades(java.util.Map<Long, Grade> gradesByEvaluationId) {
        return evaluations().stream()
                .map(e -> Evaluation.rehydrate(e.getId(), e.getCategoryId(), e.getName().value(),
                        e.getDescription().value(), e.getType(), e.getWeightWithinCategory(), e.getDueDate(),
                        e.getRubricDescription().value(), e.getCriteria(),
                        gradesByEvaluationId.containsKey(e.getId())
                                ? List.of(gradesByEvaluationId.get(e.getId()))
                                : List.of()))
                .toList();
    }

    @Test
    @DisplayName("definitiva completa: 40% × Parcial + 30% × Quiz1 + 30% × Quiz2")
    void computesCompleteWeightedFinalGrade() {
        // Parcial 1 (peso efectivo 40%) = 4.0; Quiz 1 (30%) = 5.0; Quiz 2 (30%) = 2.0
        // Final = 0.40×4.0 + 0.30×5.0 + 0.30×2.0 = 1.60 + 1.50 + 0.60 = 3.70 → APPROVED
        List<Evaluation> graded = withGrades(java.util.Map.of(
                100L, grade(1L, "4.0"),
                101L, grade(1L, "5.0"),
                102L, grade(1L, "2.0")));

        FinalGradeResult result = FinalGradeCalculator.compute(1L, course(), graded);

        assertThat(result.finalGrade()).isEqualByComparingTo(new BigDecimal("3.70"));
        assertThat(result.accumulatedWeightPct()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.status()).isEqualTo(FinalGradeStatus.APPROVED);
        assertThat(result.gradedCount()).isEqualTo(3);
        assertThat(result.pendingCount()).isEqualTo(0);
        assertThat(result.contributions()).hasSize(3);
        assertThat(result.contributions().get(0).effectiveWeightPct()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.contributions().get(1).effectiveWeightPct()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("definitiva parcial: solo calificadas suman al porcentaje acumulado")
    void computesPartialAccumulatedWeight() {
        // Solo Parcial 1 = 5.0: acumulado 40%, final 2.00, en curso
        List<Evaluation> graded = withGrades(java.util.Map.of(100L, grade(1L, "5.0")));

        FinalGradeResult result = FinalGradeCalculator.compute(1L, course(), graded);

        assertThat(result.finalGrade()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(result.accumulatedWeightPct()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.status()).isEqualTo(FinalGradeStatus.IN_PROGRESS);
        assertThat(result.gradedCount()).isEqualTo(1);
        assertThat(result.pendingCount()).isEqualTo(2);
        assertThat(result.contributions()).filteredOn(c -> !c.graded()).hasSize(2);
    }

    @Test
    @DisplayName("definitiva reprobada: completa pero inferior a 3.0")
    void rejectsFailingFinalGrade() {
        List<Evaluation> graded = withGrades(java.util.Map.of(
                100L, grade(1L, "2.0"),
                101L, grade(1L, "2.0"),
                102L, grade(1L, "2.0")));

        FinalGradeResult result = FinalGradeCalculator.compute(1L, course(), graded);

        assertThat(result.finalGrade()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(result.status()).isEqualTo(FinalGradeStatus.REJECTED);
    }

    @Test
    @DisplayName("estudiante sin notas: definitiva 0.00 y en curso")
    void handlesStudentWithoutGrades() {
        FinalGradeResult result = FinalGradeCalculator.compute(99L, course(), evaluations());

        assertThat(result.finalGrade()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.accumulatedWeightPct()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.status()).isEqualTo(FinalGradeStatus.IN_PROGRESS);
        assertThat(result.gradedCount()).isEqualTo(0);
        assertThat(result.pendingCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("curso sin evaluaciones: definitiva 0.00 y en curso")
    void handlesCourseWithoutEvaluations() {
        FinalGradeResult result = FinalGradeCalculator.compute(1L, course(), List.of());

        assertThat(result.finalGrade()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.status()).isEqualTo(FinalGradeStatus.IN_PROGRESS);
    }
}

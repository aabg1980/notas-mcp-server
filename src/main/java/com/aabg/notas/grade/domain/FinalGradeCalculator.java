package com.aabg.notas.grade.domain;

import com.aabg.notas.course.domain.Course;
import com.aabg.notas.evaluation.domain.Evaluation;
import com.aabg.notas.evaluation.domain.Grade;
import com.aabg.notas.shared.domain.WeightPercentage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de dominio puro que calcula la nota definitiva ponderada de un estudiante
 * en una asignatura.
 *
 * <p>Es un servicio de dominio (no una entidad) porque el cálculo atraviesa dos
 * agregados: la estructura ponderada vive en {@link Course} (rubros) y las notas
 * viven en {@link Evaluation}. No accede a repositorios: recibe los agregados ya
 * cargados, lo que lo hace puro y trivialmente testeable.</p>
 *
 * <p>Fórmula: definitiva = Σ (peso del rubro / 100) × (peso de la evaluación
 * dentro del rubro / 100) × nota (0.0 a 5.0), sumada solo sobre las evaluaciones
 * que ya tienen nota registrada para el estudiante. El porcentaje acumulado es la
 * suma de los pesos efectivos de las evaluaciones calificadas.</p>
 */
public final class FinalGradeCalculator {

    /** Umbral de aprobación en la escala 0.0 a 5.0. */
    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("3.00");

    private static final BigDecimal ONE_HUNDRED = WeightPercentage.MAX;

    private FinalGradeCalculator() {
    }

    /** Estado de la definitiva del estudiante en la asignatura. */
    public enum FinalGradeStatus {
        APPROVED,
        REJECTED,
        IN_PROGRESS
    }

    /** Aporte de una evaluación a la definitiva (o su peso pendiente de calificar). */
    public record GradeContribution(
            Long evaluationId,
            String categoryName,
            String evaluationName,
            BigDecimal effectiveWeightPct,
            BigDecimal score,
            BigDecimal contribution) {

        public boolean graded() {
            return score != null;
        }
    }

    /** Resultado completo del cálculo de la definitiva. */
    public record FinalGradeResult(
            Long studentId,
            BigDecimal finalGrade,
            BigDecimal accumulatedWeightPct,
            FinalGradeStatus status,
            List<GradeContribution> contributions,
            int gradedCount,
            int pendingCount) {
    }

    /**
     * Calcula la definitiva ponderada de un estudiante a partir de la estructura
     * del curso y las evaluaciones (con sus notas) ya cargadas.
     */
    public static FinalGradeResult compute(Long studentId, Course course, List<Evaluation> evaluations) {
        Map<Long, com.aabg.notas.course.domain.AssessmentCategory> categoriesById =
                course.getCategories().stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(
                                com.aabg.notas.course.domain.AssessmentCategory::getId, Function.identity()));

        List<GradeContribution> contributions = new ArrayList<>();
        BigDecimal weightedScoreSum = BigDecimal.ZERO;
        BigDecimal accumulatedWeight = BigDecimal.ZERO;
        int gradedCount = 0;

        for (Evaluation evaluation : evaluations) {
            com.aabg.notas.course.domain.AssessmentCategory category = categoriesById.get(evaluation.getCategoryId());
            BigDecimal categoryWeight = category != null ? category.getWeight().value() : BigDecimal.ZERO;
            String categoryName = category != null ? category.getName().value() : "";

            // Peso efectivo de la evaluación sobre la definitiva: (peso rubro × peso evaluación) / 100
            BigDecimal effectiveWeight = categoryWeight
                    .multiply(evaluation.getWeightWithinCategory().value())
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

            Optional<Grade> grade = evaluation.gradeForStudent(studentId);
            BigDecimal score = null;
            BigDecimal contribution = BigDecimal.ZERO;

            if (grade.isPresent()) {
                score = grade.get().getScore().value();
                contribution = effectiveWeight.multiply(score).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
                weightedScoreSum = weightedScoreSum.add(contribution);
                accumulatedWeight = accumulatedWeight.add(effectiveWeight);
                gradedCount++;
            }

            contributions.add(new GradeContribution(
                    evaluation.getId(),
                    categoryName,
                    evaluation.getName().value(),
                    effectiveWeight,
                    score,
                    contribution.setScale(2, RoundingMode.HALF_UP)));
        }

        BigDecimal finalGrade = weightedScoreSum.setScale(2, RoundingMode.HALF_UP);
        BigDecimal accumulated = accumulatedWeight.setScale(2, RoundingMode.HALF_UP);
        boolean complete = !evaluations.isEmpty()
                && accumulated.compareTo(ONE_HUNDRED) == 0
                && gradedCount == evaluations.size();

        FinalGradeStatus status;
        if (!complete) {
            status = FinalGradeStatus.IN_PROGRESS;
        } else if (finalGrade.compareTo(APPROVAL_THRESHOLD) >= 0) {
            status = FinalGradeStatus.APPROVED;
        } else {
            status = FinalGradeStatus.REJECTED;
        }

        return new FinalGradeResult(
                studentId,
                finalGrade,
                accumulated,
                status,
                List.copyOf(contributions),
                gradedCount,
                evaluations.size() - gradedCount);
    }
}

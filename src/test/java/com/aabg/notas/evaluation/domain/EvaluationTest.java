package com.aabg.notas.evaluation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las invariantes del agregado Evaluation (modelo rico).
 * Verifican que la raíz protege sus reglas de negocio sin servicios externos.
 */
class EvaluationTest {

    private static final WeightPercentage CIEN = WeightPercentage.of("100.00");
    private static final WeightPercentage CINCUENTA = WeightPercentage.of("50.00");

    @Test
    @DisplayName("create: crea la evaluación con valores por defecto válidos")
    void createAppliesDefaults() {
        Evaluation evaluation = Evaluation.create(1L, "Quiz 1", "Ecuaciones de primer orden",
                null, CIEN, LocalDate.of(2026, 3, 20), "Rúbrica general");

        assertThat(evaluation.getId()).isNull();
        assertThat(evaluation.getCategoryId()).isEqualTo(1L);
        assertThat(evaluation.getType()).isEqualTo(EvaluationType.INDIVIDUAL);
        assertThat(evaluation.getCriteria()).isEmpty();
        assertThat(evaluation.getGrades()).isEmpty();
    }

    @Test
    @DisplayName("create: rechaza evaluación sin rubro o sin nombre")
    void createRejectsInvalidData() {
        assertThatThrownBy(() -> Evaluation.create(null, "Quiz 1", null, null, CIEN, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("rubro");
        assertThatThrownBy(() -> Evaluation.create(1L, "  ", null, null, CIEN, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("GradeScore: la escala de notas 0.0 a 5.0 está garantizada por el Value Object")
    void gradeScoreGuaranteesRange() {
        assertThat(GradeScore.of("0.00").value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(GradeScore.of("5.00").value()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(GradeScore.of("2.99").isPassing()).isFalse();
        assertThat(GradeScore.of("3.00").isPassing()).isTrue();

        assertThatThrownBy(() -> GradeScore.of("5.01"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("0.0");
        assertThatThrownBy(() -> GradeScore.of("-0.5"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("recordGrade: registra la nota de un estudiante nuevo")
    void recordGradeCreatesNewGrade() {
        Evaluation evaluation = evaluationWithoutCriteria();

        Grade grade = evaluation.recordGrade(100L, GradeScore.of("4.5"), "Excelente trabajo");

        assertThat(grade.getStudentId()).isEqualTo(100L);
        assertThat(grade.getScore()).isEqualTo(GradeScore.of("4.50"));
        assertThat(grade.getFeedback().value()).isEqualTo("Excelente trabajo");
        assertThat(evaluation.getGrades()).hasSize(1);
        assertThat(grade.isPassing()).isTrue();
    }

    @Test
    @DisplayName("recordGrade: actualizar la nota de un estudiante ya calificado no duplica la nota")
    void recordGradeUpsertsExistingGrade() {
        Evaluation evaluation = evaluationWithoutCriteria();
        Grade original = evaluation.recordGrade(100L, GradeScore.of("2.0"), "Insuficiente");

        Grade updated = evaluation.recordGrade(100L, GradeScore.of("4.8"), "Corrección aceptada");

        assertThat(updated).isSameAs(original);
        assertThat(evaluation.getGrades()).hasSize(1);
        assertThat(updated.getScore()).isEqualTo(GradeScore.of("4.80"));
        assertThat(updated.getFeedback().value()).isEqualTo("Corrección aceptada");
    }

    @Test
    @DisplayName("recordGrade: el rango de la nota lo valida el Value Object, no la raíz")
    void recordGradeRangeIsGuaranteedByValueObject() {
        Evaluation evaluation = evaluationWithoutCriteria();

        assertThatThrownBy(() -> evaluation.recordGrade(100L, GradeScore.of("5.5"), null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5");
        assertThat(evaluation.getGrades()).isEmpty();
    }

    @Test
    @DisplayName("removeGrade: elimina la nota del estudiante")
    void removeGradeDeletesGrade() {
        Evaluation evaluation = evaluationWithoutCriteria();
        evaluation.recordGrade(100L, GradeScore.of("4.0"), null);

        evaluation.removeGrade(100L);

        assertThat(evaluation.gradeForStudent(100L)).isEmpty();
        assertThat(evaluation.getGrades()).isEmpty();
    }

    @Test
    @DisplayName("removeGrade: rechaza un estudiante sin nota registrada")
    void removeGradeRejectsUnknownStudent() {
        Evaluation evaluation = evaluationWithoutCriteria();

        assertThatThrownBy(() -> evaluation.removeGrade(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("addCriterion: agrega criterios de rúbrica validando la sumatoria de pesos")
    void addCriterionValidatesRubricWeights() {
        Evaluation evaluation = evaluationWithoutCriteria();

        evaluation.addCriterion("Claridad", "Solución clara", MaxPoints.of("2.5"), CINCUENTA);
        evaluation.addCriterion("Corrección", "Resultados correctos", MaxPoints.of("2.5"), CINCUENTA);

        assertThat(evaluation.getCriteria()).hasSize(2);
        assertThat(evaluation.totalRubricWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("addCriterion: rechaza la sumatoria de pesos de rúbrica que exceda el 100%")
    void addCriterionRejectsWeightsExceeding100() {
        Evaluation evaluation = evaluationWithoutCriteria();
        evaluation.addCriterion("Claridad", null, MaxPoints.of("2.5"), CINCUENTA);

        assertThatThrownBy(() -> evaluation.addCriterion("Corrección", null, MaxPoints.of("2.5"), WeightPercentage.of("60.00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("excedería el 100%");

        // El estado no se mutó: la validación ocurre antes de agregar
        assertThat(evaluation.getCriteria()).hasSize(1);
        assertThat(evaluation.totalRubricWeight()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("addCriterion: rechaza criterios duplicados")
    void addCriterionRejectsDuplicates() {
        Evaluation evaluation = evaluationWithCriterion("Claridad", "20.00");

        assertThatThrownBy(() -> evaluation.addCriterion("claridad", null, MaxPoints.of("1.0"), WeightPercentage.of("20.00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene un criterio");
    }

    @Test
    @DisplayName("removeCriterion: elimina un criterio existente y libera su peso")
    void removeCriterionFreesWeight() {
        Evaluation evaluation = evaluationWithCriteria(
                RubricCriterion.rehydrate(20L, "Claridad", "Solución clara", MaxPoints.of("2.50"), CINCUENTA),
                RubricCriterion.rehydrate(21L, "Corrección", "Resultados correctos", MaxPoints.of("2.50"), CINCUENTA));

        evaluation.removeCriterion(20L);

        assertThat(evaluation.getCriteria()).hasSize(1);
        assertThat(evaluation.totalRubricWeight()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("averageScore: calcula el promedio de las notas registradas")
    void averageScoreComputesMean() {
        Evaluation evaluation = evaluationWithoutCriteria();
        assertThat(evaluation.averageScore()).isEmpty();

        evaluation.recordGrade(100L, GradeScore.of("4.0"), null);
        evaluation.recordGrade(200L, GradeScore.of("3.0"), null);

        assertThat(evaluation.averageScore()).contains(GradeScore.of("3.50"));
        assertThat(evaluation.passingGradeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getCriteria y getGrades: las colecciones son de solo lectura para el exterior")
    void internalCollectionsAreReadOnly() {
        Evaluation evaluation = evaluationWithoutCriteria();
        evaluation.recordGrade(100L, GradeScore.of("4.0"), null);

        assertThatThrownBy(() -> evaluation.getGrades().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> evaluation.getCriteria().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("igualdad de identidad por (rubro, nombre)")
    void identityIsCategoryAndName() {
        Evaluation first = Evaluation.create(1L, "Quiz 1", null, null, CIEN, null, null);
        Evaluation second = Evaluation.create(1L, "Quiz 1", "otra descripción", EvaluationType.GROUP, CIEN, null, null);
        Evaluation other = Evaluation.create(1L, "Quiz 2", null, null, CIEN, null, null);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(other);
    }

    /**
     * Construye una evaluación con identidad persistida (id = 1), sin criterios
     * ni notas, simulando el agregado reconstruido desde la persistencia.
     */
    private Evaluation evaluationWithoutCriteria() {
        return Evaluation.rehydrate(
                1L, 10L, "Quiz 1", "Ecuaciones de primer orden", EvaluationType.INDIVIDUAL,
                CIEN, LocalDate.of(2026, 3, 20), "Rúbrica general",
                List.of(), List.of());
    }

    /** Igual que {@link #evaluationWithoutCriteria()} pero con un criterio precargado con identidad. */
    private Evaluation evaluationWithCriterion(String name, String weight) {
        RubricCriterion existing = RubricCriterion.rehydrate(20L, name, "Criterio precargado",
                MaxPoints.of("2.50"), WeightPercentage.of(weight));
        return Evaluation.rehydrate(
                1L, 10L, "Quiz 1", "Ecuaciones de primer orden", EvaluationType.INDIVIDUAL,
                CIEN, LocalDate.of(2026, 3, 20), "Rúbrica general",
                List.of(existing), List.of());
    }

    /** Igual que {@link #evaluationWithoutCriteria()} pero con varios criterios precargados con identidad. */
    private Evaluation evaluationWithCriteria(RubricCriterion... criteria) {
        return Evaluation.rehydrate(
                1L, 10L, "Quiz 1", "Ecuaciones de primer orden", EvaluationType.INDIVIDUAL,
                CIEN, LocalDate.of(2026, 3, 20), "Rúbrica general",
                List.of(criteria), List.of());
    }
}

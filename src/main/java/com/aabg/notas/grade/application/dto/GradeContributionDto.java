package com.aabg.notas.grade.application.dto;

import java.math.BigDecimal;

/**
 * DTO de transferencia (Java record inmutable) con el detalle del aporte de una
 * evaluación a la definitiva ponderada.
 */
public record GradeContributionDto(
        Long evaluationId,
        String categoryName,
        String evaluationName,
        Double effectiveWeightPct,
        Double score,
        Double contribution,
        Boolean graded) {
}

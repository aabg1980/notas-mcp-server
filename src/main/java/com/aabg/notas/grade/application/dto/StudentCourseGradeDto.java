package com.aabg.notas.grade.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de transferencia (Java record inmutable) con una nota individual de un
 * estudiante dentro de una asignatura.
 */
public record StudentCourseGradeDto(
        Long evaluationId,
        String categoryName,
        String evaluationName,
        Double effectiveWeightPct,
        Double score,
        String feedback,
        LocalDateTime gradedAt,
        Boolean passing) {
}

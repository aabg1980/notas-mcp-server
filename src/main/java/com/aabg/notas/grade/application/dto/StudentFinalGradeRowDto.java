package com.aabg.notas.grade.application.dto;

import java.math.BigDecimal;

/**
 * DTO de transferencia (Java record inmutable) con la definitiva de un estudiante
 * dentro del reporte consolidado de una asignatura.
 */
public record StudentFinalGradeRowDto(
        String studentCode,
        String studentFullName,
        BigDecimal finalGrade,
        BigDecimal accumulatedWeightPct,
        String status) {
}

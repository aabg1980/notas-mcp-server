package com.aabg.notas.grade.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de transferencia (Java record inmutable) con el reporte de la nota definitiva
 * ponderada de un estudiante en una asignatura.
 */
public record FinalGradeReportDto(
        String studentCode,
        String studentFullName,
        String courseCode,
        String courseName,
        String academicPeriod,
        String groupId,
        BigDecimal finalGrade,
        BigDecimal accumulatedWeightPct,
        String status,
        Integer gradedCount,
        Integer pendingCount,
        List<GradeContributionDto> detail) {
}

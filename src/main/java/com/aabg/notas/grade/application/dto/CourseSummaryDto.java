package com.aabg.notas.grade.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de transferencia (Java record inmutable) con el reporte consolidado de las
 * definitivas de todos los estudiantes matriculados en una asignatura.
 */
public record CourseSummaryDto(
        String courseCode,
        String courseName,
        String academicPeriod,
        String groupId,
        Integer enrolledStudents,
        Integer gradedStudents,
        Integer approvedCount,
        Integer rejectedCount,
        Integer inProgressCount,
        List<StudentFinalGradeRowDto> rows) {
}

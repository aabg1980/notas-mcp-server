package com.aabg.notas.course.application.dto;

import java.util.List;

/**
 * DTO de transferencia (Java record inmutable) que expone una asignatura (agregado
 * completo, con sus rubros) hacia los adaptadores de entrada (herramientas MCP).
 */
public record CourseDto(
        Long id,
        String code,
        String name,
        String academicPeriod,
        String groupId,
        String status,
        Double totalWeightPercentage,
        List<AssessmentCategoryDto> categories) {
}

package com.aabg.notas.evaluation.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de transferencia (Java record inmutable) que expone una evaluación (agregado
 * completo, con su rúbrica y notas) hacia los adaptadores de entrada (herramientas MCP).
 */
public record EvaluationDto(
        Long id,
        Long categoryId,
        String name,
        String description,
        String type,
        Double weightWithinCategory,
        LocalDate dueDate,
        String rubricDescription,
        List<RubricCriterionDto> criteria,
        List<GradeDto> grades) {
}

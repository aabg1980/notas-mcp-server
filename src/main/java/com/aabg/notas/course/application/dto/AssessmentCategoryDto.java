package com.aabg.notas.course.application.dto;

/**
 * DTO de transferencia (Java record inmutable) que expone un rubro de evaluación
 * de una asignatura hacia los adaptadores de entrada (herramientas MCP).
 */
public record AssessmentCategoryDto(
        Long id,
        String name,
        String description,
        Double weightPercentage) {
}

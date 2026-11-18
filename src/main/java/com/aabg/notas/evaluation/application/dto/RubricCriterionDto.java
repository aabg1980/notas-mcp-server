package com.aabg.notas.evaluation.application.dto;

/**
 * DTO de transferencia (Java record inmutable) que expone un criterio de rúbrica
 * hacia los adaptadores de entrada (herramientas MCP).
 */
public record RubricCriterionDto(
        Long id,
        String name,
        String description,
        Double maxPoints,
        Double weightPercentage) {
}

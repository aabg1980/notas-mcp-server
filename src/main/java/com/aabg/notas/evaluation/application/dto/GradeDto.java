package com.aabg.notas.evaluation.application.dto;

import java.time.LocalDateTime;

/**
 * DTO de transferencia (Java record inmutable) que expone la nota de un estudiante
 * en una evaluación hacia los adaptadores de entrada (herramientas MCP).
 */
public record GradeDto(
        Long id,
        Long studentId,
        String studentCode,
        String studentFullName,
        Double score,
        String feedback,
        LocalDateTime gradedAt,
        Boolean passing) {
}

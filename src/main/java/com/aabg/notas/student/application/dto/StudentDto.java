package com.aabg.notas.student.application.dto;

/**
 * DTO de transferencia (Java record inmutable) que expone un estudiante hacia
 * los adaptadores de entrada (herramientas MCP).
 */
public record StudentDto(
        Long id,
        String code,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String status) {
}

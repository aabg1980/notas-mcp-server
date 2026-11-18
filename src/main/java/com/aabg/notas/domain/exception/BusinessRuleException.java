package com.aabg.notas.domain.exception;

/**
 * Lanzada ante violaciones de invariantes o reglas del negocio educativo:
 * - Notas fuera del rango 0.0 a 5.0
 * - Porcentajes de evaluación que excedan el 100% de la asignatura
 * - Duplicidad de notas por evaluación
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}

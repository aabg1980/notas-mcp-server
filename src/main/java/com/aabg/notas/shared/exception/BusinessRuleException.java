package com.aabg.notas.shared.exception;

/**
 * Lanzada ante violaciones de invariantes o reglas del negocio educativo:
 * - Notas fuera del rango 0.0 a 5.0
 * - Porcentajes de rubros o evaluaciones que excedan el 100%
 * - Acciones sobre estudiantes inactivos o inscripciones duplicadas
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.aabg.notas.shared.exception;

/**
 * Excepción base para todas las anomalías y violaciones de reglas en el dominio académico.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

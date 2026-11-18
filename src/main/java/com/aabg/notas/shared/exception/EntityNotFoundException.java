package com.aabg.notas.shared.exception;

/**
 * Lanzada cuando una entidad del dominio no existe (estudiante, curso, evaluación, etc.).
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s no encontrado con el identificador: '%s'", entityName, identifier));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}

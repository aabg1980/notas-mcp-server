package com.aabg.notas.course.domain;

/**
 * Estado del ciclo de vida de una asignatura dentro del agregado Course.
 * Una asignatura cerrada no admite modificaciones en su estructura de rubros.
 */
public enum CourseStatus {
    OPEN,
    CLOSED
}

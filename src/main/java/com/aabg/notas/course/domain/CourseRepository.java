package com.aabg.notas.course.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Course. La capa de dominio define el contrato;
 * la infraestructura lo implementa con SQLite + Spring Data JPA.
 */
public interface CourseRepository {

    /** Persiste el agregado completo (raíz y rubros) y devuelve la versión con identidad asignada. */
    Course save(Course course);

    Optional<Course> findById(Long id);

    /** Busca la asignatura por su clave única de negocio (código + periodo + grupo). */
    Optional<Course> findByCodePeriodAndGroup(String code, String academicPeriod, String groupId);

    List<Course> findAll();

    List<Course> findByAcademicPeriod(String academicPeriod);

    List<Course> findByCode(String code);
}

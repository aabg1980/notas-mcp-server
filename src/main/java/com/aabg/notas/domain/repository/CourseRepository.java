package com.aabg.notas.domain.repository;

import com.aabg.notas.domain.model.Course;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio para la gestión y consulta de asignaturas y grupos.
 */
public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(Long id);

    Optional<Course> findByCodeAndPeriodAndGroup(String code, String academicPeriod, String groupId);

    List<Course> findAll();

    List<Course> findByAcademicPeriod(String academicPeriod);

    List<Course> findByCode(String code);
}

package com.aabg.notas.domain.repository;

import com.aabg.notas.domain.model.Student;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio para la persistencia y consulta de estudiantes.
 */
public interface StudentRepository {

    Student save(Student student);

    Optional<Student> findById(Long id);

    Optional<Student> findByCode(String code);

    Optional<Student> findByEmail(String email);

    List<Student> findAll();

    List<Student> findByCourseId(Long courseId);

    void enrollStudentInCourse(Long studentId, Long courseId);

    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
}

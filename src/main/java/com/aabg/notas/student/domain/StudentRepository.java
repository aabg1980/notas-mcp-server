package com.aabg.notas.student.domain;

import java.util.List;
import java.util.Optional;

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

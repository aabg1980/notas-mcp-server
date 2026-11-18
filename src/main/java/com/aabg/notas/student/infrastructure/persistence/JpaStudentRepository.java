package com.aabg.notas.student.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaStudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByCode(String code);

    Optional<StudentEntity> findByEmail(String email);

    /** Estudiantes matriculados en una asignatura (a través de course_enrollments). */
    @Query("SELECT s FROM StudentEntity s, CourseEnrollmentEntity e "
            + "WHERE e.studentId = s.id AND e.courseId = :courseId "
            + "ORDER BY s.lastName, s.firstName")
    List<StudentEntity> findByCourseId(@Param("courseId") Long courseId);
}

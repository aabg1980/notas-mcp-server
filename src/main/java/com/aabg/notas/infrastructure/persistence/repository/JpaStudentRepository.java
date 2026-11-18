package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.StudentEntity;
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

    @Query("""
        SELECT s FROM StudentEntity s
        JOIN CourseEnrollmentEntity e ON e.studentId = s.id
        WHERE e.courseId = :courseId
        ORDER BY s.lastName, s.firstName
    """)
    List<StudentEntity> findStudentsByCourseId(@Param("courseId") Long courseId);
}

package com.aabg.notas.student.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, Long> {

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}

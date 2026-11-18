package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.CourseEnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, Long> {

    Optional<CourseEnrollmentEntity> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseEnrollmentEntity> findByCourseId(Long courseId);

    List<CourseEnrollmentEntity> findByStudentId(Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}

package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCourseRepository extends JpaRepository<CourseEntity, Long> {

    Optional<CourseEntity> findByCodeAndAcademicPeriodAndGroupId(String code, String academicPeriod, String groupId);

    List<CourseEntity> findByAcademicPeriod(String academicPeriod);

    List<CourseEntity> findByCode(String code);
}

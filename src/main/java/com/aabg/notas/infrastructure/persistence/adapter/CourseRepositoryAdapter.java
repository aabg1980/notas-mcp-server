package com.aabg.notas.infrastructure.persistence.adapter;

import com.aabg.notas.domain.model.Course;
import com.aabg.notas.domain.repository.CourseRepository;
import com.aabg.notas.infrastructure.persistence.entity.CourseEntity;
import com.aabg.notas.infrastructure.persistence.repository.JpaCourseRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class CourseRepositoryAdapter implements CourseRepository {

    private final JpaCourseRepository jpaCourseRepository;

    public CourseRepositoryAdapter(JpaCourseRepository jpaCourseRepository) {
        this.jpaCourseRepository = jpaCourseRepository;
    }

    @Override
    public Course save(Course course) {
        CourseEntity entity = toEntity(course);
        CourseEntity saved = jpaCourseRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaCourseRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Course> findByCodeAndPeriodAndGroup(String code, String academicPeriod, String groupId) {
        return jpaCourseRepository.findByCodeAndAcademicPeriodAndGroupId(
                code.toUpperCase(), academicPeriod, groupId.toUpperCase()
        ).map(this::toDomain);
    }

    @Override
    public List<Course> findAll() {
        return jpaCourseRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByAcademicPeriod(String academicPeriod) {
        return jpaCourseRepository.findByAcademicPeriod(academicPeriod).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByCode(String code) {
        return jpaCourseRepository.findByCode(code.toUpperCase()).stream()
                .map(this::toDomain)
                .toList();
    }

    private CourseEntity toEntity(Course domain) {
        return new CourseEntity(
                domain.getId(),
                domain.getCode(),
                domain.getName(),
                domain.getAcademicPeriod(),
                domain.getGroupId()
        );
    }

    private Course toDomain(CourseEntity entity) {
        return new Course(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAcademicPeriod(),
                entity.getGroupId()
        );
    }
}

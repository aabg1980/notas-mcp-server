package com.aabg.notas.infrastructure.persistence.adapter;

import com.aabg.notas.domain.model.Student;
import com.aabg.notas.domain.model.StudentStatus;
import com.aabg.notas.domain.repository.StudentRepository;
import com.aabg.notas.infrastructure.persistence.entity.CourseEnrollmentEntity;
import com.aabg.notas.infrastructure.persistence.entity.StudentEntity;
import com.aabg.notas.infrastructure.persistence.repository.JpaCourseEnrollmentRepository;
import com.aabg.notas.infrastructure.persistence.repository.JpaStudentRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class StudentRepositoryAdapter implements StudentRepository {

    private final JpaStudentRepository jpaStudentRepository;
    private final JpaCourseEnrollmentRepository jpaEnrollmentRepository;

    public StudentRepositoryAdapter(JpaStudentRepository jpaStudentRepository,
                                    JpaCourseEnrollmentRepository jpaEnrollmentRepository) {
        this.jpaStudentRepository = jpaStudentRepository;
        this.jpaEnrollmentRepository = jpaEnrollmentRepository;
    }

    @Override
    public Student save(Student student) {
        StudentEntity entity = toEntity(student);
        StudentEntity saved = jpaStudentRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return jpaStudentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Student> findByCode(String code) {
        return jpaStudentRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return jpaStudentRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Student> findAll() {
        return jpaStudentRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Student> findByCourseId(Long courseId) {
        return jpaStudentRepository.findStudentsByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        if (!jpaEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            jpaEnrollmentRepository.save(new CourseEnrollmentEntity(null, courseId, studentId));
        }
    }

    @Override
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return jpaEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    private StudentEntity toEntity(Student domain) {
        return new StudentEntity(
                domain.getId(),
                domain.getCode(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getStatus().name()
        );
    }

    private Student toDomain(StudentEntity entity) {
        StudentStatus status = StudentStatus.valueOf(entity.getStatus());
        return new Student(
                entity.getId(),
                entity.getCode(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                status
        );
    }
}

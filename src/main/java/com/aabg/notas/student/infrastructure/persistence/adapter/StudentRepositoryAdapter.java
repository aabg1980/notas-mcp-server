package com.aabg.notas.student.infrastructure.persistence.adapter;

import com.aabg.notas.student.domain.Student;
import com.aabg.notas.student.domain.StudentRepository;
import com.aabg.notas.student.domain.StudentStatus;
import com.aabg.notas.student.infrastructure.persistence.CourseEnrollmentEntity;
import com.aabg.notas.student.infrastructure.persistence.JpaCourseEnrollmentRepository;
import com.aabg.notas.student.infrastructure.persistence.JpaStudentRepository;
import com.aabg.notas.student.infrastructure.persistence.StudentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia (SQLite + Spring Data JPA) del puerto StudentRepository.
 * Gestiona tanto la raíz Student como la matrícula de estudiantes en asignaturas
 * (course_enrollments), operación idempotente.
 */
@Component
public class StudentRepositoryAdapter implements StudentRepository {

    private final JpaStudentRepository jpaStudentRepository;
    private final JpaCourseEnrollmentRepository jpaCourseEnrollmentRepository;

    public StudentRepositoryAdapter(JpaStudentRepository jpaStudentRepository,
                                    JpaCourseEnrollmentRepository jpaCourseEnrollmentRepository) {
        this.jpaStudentRepository = jpaStudentRepository;
        this.jpaCourseEnrollmentRepository = jpaCourseEnrollmentRepository;
    }

    @Override
    @Transactional
    public Student save(Student student) {
        StudentEntity saved = jpaStudentRepository.save(toEntity(student));
        return toDomain(saved);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return jpaStudentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Student> findByCode(String code) {
        return jpaStudentRepository.findByCode(normalizeCode(code)).map(this::toDomain);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return jpaStudentRepository.findByEmail(email == null ? "" : email.trim().toLowerCase())
                .map(this::toDomain);
    }

    @Override
    public List<Student> findAll() {
        return jpaStudentRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Student> findByCourseId(Long courseId) {
        return jpaStudentRepository.findByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        if (!jpaCourseEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            jpaCourseEnrollmentRepository.save(new CourseEnrollmentEntity(null, courseId, studentId));
        }
    }

    @Override
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return jpaCourseEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    // ==================== Mapeo dominio <-> persistencia ====================

    private StudentEntity toEntity(Student domain) {
        return new StudentEntity(
                domain.getId(),
                domain.getCode(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getStatus().name());
    }

    private Student toDomain(StudentEntity entity) {
        return Student.rehydrate(
                entity.getId(),
                entity.getCode(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                StudentStatus.valueOf(entity.getStatus()));
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}

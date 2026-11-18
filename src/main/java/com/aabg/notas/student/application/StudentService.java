package com.aabg.notas.student.application;

import com.aabg.notas.course.domain.CourseRepository;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import com.aabg.notas.student.application.dto.StudentDto;
import com.aabg.notas.student.domain.Student;
import com.aabg.notas.student.domain.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación (casos de uso) del módulo Student.
 *
 * <p>Con el modelo rico, este servicio solo orquesta: carga agregados por sus
 * puertos, invoca comportamiento del dominio y persiste. Las reglas de negocio
 * viven en el agregado, no aquí.</p>
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public StudentDto registerStudent(String code, String firstName, String lastName, String email) {
        if (studentRepository.findByCode(code).isPresent()) {
            throw new BusinessRuleException(String.format("Ya existe un estudiante con el código '%s'", code));
        }
        if (studentRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException(String.format("Ya existe un estudiante con el correo '%s'", email));
        }
        Student student = studentRepository.save(Student.create(code, firstName, lastName, email));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public List<StudentDto> listStudents() {
        return studentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByCode(String code) {
        return toDto(requireStudent(code));
    }

    @Transactional
    public StudentDto updatePersonalInformation(String code, String newFirstName, String newLastName, String newEmail) {
        Student student = requireStudent(code);
        studentRepository.findByEmail(newEmail)
                .filter(other -> !other.getId().equals(student.getId()))
                .ifPresent(other -> {
                    throw new BusinessRuleException(String.format(
                            "El correo '%s' ya pertenece al estudiante '%s'", newEmail, other.getCode()));
                });
        student.updatePersonalInformation(newFirstName, newLastName, newEmail);
        return toDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto activateStudent(String code) {
        Student student = requireStudent(code);
        student.activate();
        return toDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto deactivateStudent(String code) {
        Student student = requireStudent(code);
        student.deactivate();
        return toDto(studentRepository.save(student));
    }

    /** Matricula un estudiante activo en una asignatura (referencias cruzadas por identidad). */
    @Transactional
    public StudentDto enrollStudentInCourse(String studentCode, String courseCode, String academicPeriod,
                                            String groupId) {
        Student student = requireStudent(studentCode);
        student.validateCanBeEvaluated();

        var course = courseRepository.findByCodePeriodAndGroup(courseCode, academicPeriod, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura",
                        courseCode + "-" + academicPeriod + "-" + groupId));

        if (studentRepository.isStudentEnrolledInCourse(student.getId(), course.getId())) {
            throw new BusinessRuleException(String.format(
                    "El estudiante '%s' ya está matriculado en la asignatura '%s'",
                    studentCode, course.getUniqueKey()));
        }

        studentRepository.enrollStudentInCourse(student.getId(), course.getId());
        return toDto(student);
    }

    // ==================== Soporte interno ====================

    private Student requireStudent(String code) {
        return studentRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante", code));
    }

    private StudentDto toDto(Student student) {
        return new StudentDto(
                student.getId(),
                student.getCode(),
                student.getFirstName(),
                student.getLastName(),
                student.getFullName(),
                student.getEmail(),
                student.getStatus().name());
    }
}

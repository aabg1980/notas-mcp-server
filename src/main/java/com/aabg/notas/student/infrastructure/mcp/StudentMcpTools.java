package com.aabg.notas.student.infrastructure.mcp;

import com.aabg.notas.student.application.StudentService;
import com.aabg.notas.student.application.dto.StudentDto;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada MCP del módulo Student. Expone herramientas fuertemente
 * tipadas al LLM; delega exclusivamente en los casos de uso de la capa de aplicación.
 */
@Component
public class StudentMcpTools {

    private final StudentService studentService;

    public StudentMcpTools(StudentService studentService) {
        this.studentService = studentService;
    }

    @Tool(name = "add_student", description = "Registra un nuevo estudiante con código único, nombres, apellidos y correo institucional.")
    public StudentDto addStudent(
            @ToolParam(description = "Código único del estudiante (ej. EST-001)") String code,
            @ToolParam(description = "Nombres del estudiante") String firstName,
            @ToolParam(description = "Apellidos del estudiante") String lastName,
            @ToolParam(description = "Correo electrónico institucional") String email) {
        return studentService.registerStudent(code, firstName, lastName, email);
    }

    @Tool(name = "list_students", description = "Retorna todos los estudiantes registrados en el sistema.")
    public List<StudentDto> listStudents() {
        return studentService.listStudents();
    }

    @Tool(name = "get_student", description = "Busca un estudiante por su código único.")
    public StudentDto getStudent(
            @ToolParam(description = "Código único del estudiante (ej. EST-001)") String code) {
        return studentService.getStudentByCode(code);
    }

    @Tool(name = "update_student", description = "Actualiza los datos personales de un estudiante.")
    public StudentDto updateStudent(
            @ToolParam(description = "Código único del estudiante") String code,
            @ToolParam(description = "Nuevos nombres del estudiante") String firstName,
            @ToolParam(description = "Nuevos apellidos del estudiante") String lastName,
            @ToolParam(description = "Nuevo correo electrónico") String email) {
        return studentService.updatePersonalInformation(code, firstName, lastName, email);
    }

    @Tool(name = "activate_student", description = "Activa un estudiante inactivo para permitir matrícula y calificaciones.")
    public StudentDto activateStudent(
            @ToolParam(description = "Código único del estudiante") String code) {
        return studentService.activateStudent(code);
    }

    @Tool(name = "deactivate_student", description = "Desactiva un estudiante: no podrá matricularse ni recibir calificaciones.")
    public StudentDto deactivateStudent(
            @ToolParam(description = "Código único del estudiante") String code) {
        return studentService.deactivateStudent(code);
    }

    @Tool(name = "enroll_student", description = "Matricula un estudiante activo en una asignatura (periodo y grupo).")
    public StudentDto enrollStudent(
            @ToolParam(description = "Código único del estudiante") String studentCode,
            @ToolParam(description = "Código de la asignatura (ej. MAT101)") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return studentService.enrollStudentInCourse(studentCode, courseCode, academicPeriod, groupId);
    }
}

package com.aabg.notas.grade.infrastructure.mcp;

import com.aabg.notas.grade.application.GradeCalculationService;
import com.aabg.notas.grade.application.dto.CourseSummaryDto;
import com.aabg.notas.grade.application.dto.FinalGradeReportDto;
import com.aabg.notas.grade.application.dto.StudentCourseGradeDto;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada MCP del módulo de cálculo de notas. Expone herramientas
 * fuertemente tipadas al LLM; delega en el servicio de cálculo, que a su vez usa
 * el servicio de dominio puro FinalGradeCalculator.
 */
@Component
public class GradeMcpTools {

    private final GradeCalculationService gradeCalculationService;

    public GradeMcpTools(GradeCalculationService gradeCalculationService) {
        this.gradeCalculationService = gradeCalculationService;
    }

    @Tool(name = "calculate_final_grade", description = "Calcula la nota definitiva ponderada de un estudiante en una asignatura: (peso rubro × peso evaluación × nota). Incluye porcentaje acumulado y estado (APPROVED/REJECTED/IN_PROGRESS).")
    public FinalGradeReportDto calculateFinalGrade(
            @ToolParam(description = "Código único del estudiante") String studentCode,
            @ToolParam(description = "Código de la asignatura") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return gradeCalculationService.calculateFinalGrade(studentCode, courseCode, academicPeriod, groupId);
    }

    @Tool(name = "get_course_summary", description = "Genera el reporte consolidado de definitivas de todos los estudiantes matriculados en una asignatura, ordenado por nota.")
    public CourseSummaryDto getCourseSummary(
            @ToolParam(description = "Código de la asignatura") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return gradeCalculationService.getCourseSummary(courseCode, academicPeriod, groupId);
    }

    @Tool(name = "get_student_grades", description = "Consulta el historial de notas de un estudiante en una asignatura, con el peso de cada evaluación y su retroalimentación.")
    public List<StudentCourseGradeDto> getStudentGrades(
            @ToolParam(description = "Código único del estudiante") String studentCode,
            @ToolParam(description = "Código de la asignatura") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return gradeCalculationService.getStudentGrades(studentCode, courseCode, academicPeriod, groupId);
    }
}

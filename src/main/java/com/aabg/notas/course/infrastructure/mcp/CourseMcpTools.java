package com.aabg.notas.course.infrastructure.mcp;

import com.aabg.notas.course.application.CourseService;
import com.aabg.notas.course.application.dto.CourseDto;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada MCP del módulo Course. Expone herramientas fuertemente
 * tipadas al LLM; delega exclusivamente en los casos de uso de la capa de aplicación.
 */
@Component
public class CourseMcpTools {

    private final CourseService courseService;

    public CourseMcpTools(CourseService courseService) {
        this.courseService = courseService;
    }

    @Tool(name = "create_course", description = "Registra una nueva asignatura para un periodo académico y grupo.")
    public CourseDto createCourse(
            @ToolParam(description = "Código de la asignatura (ej. MAT101)") String code,
            @ToolParam(description = "Nombre de la asignatura") String name,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return courseService.createCourse(code, name, academicPeriod, groupId);
    }

    @Tool(name = "list_courses", description = "Lista todas las asignaturas registradas con sus rubros.")
    public List<CourseDto> listCourses() {
        return courseService.listCourses();
    }

    @Tool(name = "get_course", description = "Busca una asignatura por código, periodo académico y grupo.")
    public CourseDto getCourse(
            @ToolParam(description = "Código de la asignatura (ej. MAT101)") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return courseService.getCourse(code, academicPeriod, groupId);
    }

    @Tool(name = "add_category", description = "Agrega un rubro de evaluación (ej. Quices 20%) a una asignatura. La sumatoria de pesos de rubros no puede exceder el 100%.")
    public CourseDto addCategory(
            @ToolParam(description = "Código de la asignatura") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId,
            @ToolParam(description = "Nombre del rubro (ej. Quices)") String name,
            @ToolParam(description = "Descripción del rubro (opcional)", required = false) String description,
            @ToolParam(description = "Peso porcentual del rubro (0-100)") Double weightPercentage) {
        return courseService.addCategory(code, academicPeriod, groupId, name, description, weightPercentage);
    }

    @Tool(name = "update_category_weight", description = "Actualiza el peso porcentual de un rubro de una asignatura abierta.")
    public CourseDto updateCategoryWeight(
            @ToolParam(description = "Código de la asignatura") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId,
            @ToolParam(description = "ID del rubro a actualizar") Long categoryId,
            @ToolParam(description = "Nuevo peso porcentual (0-100)") Double weightPercentage) {
        return courseService.updateCategoryWeight(code, academicPeriod, groupId, categoryId, weightPercentage);
    }

    @Tool(name = "remove_category", description = "Elimina un rubro de una asignatura abierta, siempre que no tenga evaluaciones registradas.")
    public CourseDto removeCategory(
            @ToolParam(description = "Código de la asignatura") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId,
            @ToolParam(description = "ID del rubro a eliminar") Long categoryId) {
        return courseService.removeCategory(code, academicPeriod, groupId, categoryId);
    }

    @Tool(name = "close_course", description = "Cierra una asignatura: su estructura de rubros queda congelada.")
    public CourseDto closeCourse(
            @ToolParam(description = "Código de la asignatura") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return courseService.closeCourse(code, academicPeriod, groupId);
    }

    @Tool(name = "reopen_course", description = "Reabre una asignatura cerrada para modificar su estructura de rubros.")
    public CourseDto reopenCourse(
            @ToolParam(description = "Código de la asignatura") String code,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return courseService.reopenCourse(code, academicPeriod, groupId);
    }
}

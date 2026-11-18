package com.aabg.notas.evaluation.infrastructure.mcp;

import com.aabg.notas.evaluation.application.EvaluationService;
import com.aabg.notas.evaluation.application.dto.EvaluationDto;
import com.aabg.notas.evaluation.application.dto.GradeDto;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada MCP del módulo Evaluation. Expone herramientas fuertemente
 * tipadas al LLM; delega exclusivamente en los casos de uso de la capa de aplicación.
 */
@Component
public class EvaluationMcpTools {

    private final EvaluationService evaluationService;

    public EvaluationMcpTools(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Tool(name = "create_evaluation", description = "Crea una prueba/evaluación dentro de un rubro de una asignatura abierta (ej. Quiz 1, peso 50% dentro del rubro Quices).")
    public EvaluationDto createEvaluation(
            @ToolParam(description = "Código de la asignatura") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId,
            @ToolParam(description = "Nombre del rubro al que pertenece (ej. Quices)") String categoryName,
            @ToolParam(description = "Nombre de la evaluación (ej. Quiz 1)") String name,
            @ToolParam(description = "Descripción o temario (opcional)", required = false) String description,
            @ToolParam(description = "Modalidad: INDIVIDUAL o GROUP (por defecto INDIVIDUAL)", required = false) String type,
            @ToolParam(description = "Peso de la evaluación dentro del rubro (0-100)") Double weightWithinCategory,
            @ToolParam(description = "Fecha límite AAAA-MM-DD (opcional)", required = false) String dueDate,
            @ToolParam(description = "Descripción general de la rúbrica (opcional)", required = false) String rubricDescription) {
        return evaluationService.createEvaluation(courseCode, academicPeriod, groupId, categoryName, name,
                description, type, weightWithinCategory, dueDate, rubricDescription);
    }

    @Tool(name = "list_evaluations", description = "Lista las evaluaciones de una asignatura con su rúbrica y notas.")
    public List<EvaluationDto> listEvaluations(
            @ToolParam(description = "Código de la asignatura") String courseCode,
            @ToolParam(description = "Periodo académico (ej. 2027-1)") String academicPeriod,
            @ToolParam(description = "ID de grupo (ej. G01)") String groupId) {
        return evaluationService.listEvaluations(courseCode, academicPeriod, groupId);
    }

    @Tool(name = "get_evaluation", description = "Busca una evaluación por su ID.")
    public EvaluationDto getEvaluation(
            @ToolParam(description = "ID de la evaluación") Long evaluationId) {
        return evaluationService.getEvaluation(evaluationId);
    }

    @Tool(name = "add_criterion", description = "Agrega un criterio a la rúbrica de una evaluación. La sumatoria de pesos de criterios no puede exceder el 100%.")
    public EvaluationDto addCriterion(
            @ToolParam(description = "ID de la evaluación") Long evaluationId,
            @ToolParam(description = "Nombre del criterio (ej. Claridad)") String name,
            @ToolParam(description = "Descripción del criterio (opcional)", required = false) String description,
            @ToolParam(description = "Puntaje máximo del criterio (ej. 2.5)") Double maxPoints,
            @ToolParam(description = "Peso porcentual del criterio dentro de la rúbrica (0-100)") Double weightPercentage) {
        return evaluationService.addCriterion(evaluationId, name, description, maxPoints, weightPercentage);
    }

    @Tool(name = "remove_criterion", description = "Elimina un criterio de la rúbrica de una evaluación.")
    public EvaluationDto removeCriterion(
            @ToolParam(description = "ID de la evaluación") Long evaluationId,
            @ToolParam(description = "ID del criterio a eliminar") Long criterionId) {
        return evaluationService.removeCriterion(evaluationId, criterionId);
    }

    @Tool(name = "record_grade", description = "Asienta (o actualiza) la calificación de un estudiante activo en una evaluación. Escala 0.0 a 5.0. Un estudiante tiene una sola nota por evaluación.")
    public GradeDto recordGrade(
            @ToolParam(description = "Código único del estudiante") String studentCode,
            @ToolParam(description = "ID de la evaluación") Long evaluationId,
            @ToolParam(description = "Calificación obtenida (0.0 a 5.0)") Double score,
            @ToolParam(description = "Retroalimentación u observaciones (opcional)", required = false) String feedback) {
        return evaluationService.recordGrade(studentCode, evaluationId, score, feedback);
    }

    @Tool(name = "list_grades", description = "Lista todas las notas registradas en una evaluación.")
    public List<GradeDto> listGrades(
            @ToolParam(description = "ID de la evaluación") Long evaluationId) {
        return evaluationService.listGrades(evaluationId);
    }
}

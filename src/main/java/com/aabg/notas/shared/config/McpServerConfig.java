package com.aabg.notas.shared.config;

import com.aabg.notas.course.infrastructure.mcp.CourseMcpTools;
import com.aabg.notas.evaluation.infrastructure.mcp.EvaluationMcpTools;
import com.aabg.notas.grade.infrastructure.mcp.GradeMcpTools;
import com.aabg.notas.student.infrastructure.mcp.StudentMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del servidor MCP para Spring AI: registra las herramientas
 * (@Tool) de los cuatro módulos del dominio académico para que el LLM pueda
 * descubrirlas e invocarlas mediante el protocolo MCP.
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider notasToolCallbackProvider(StudentMcpTools studentMcpTools,
                                                         CourseMcpTools courseMcpTools,
                                                         EvaluationMcpTools evaluationMcpTools,
                                                         GradeMcpTools gradeMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(studentMcpTools, courseMcpTools, evaluationMcpTools, gradeMcpTools)
                .build();
    }
}

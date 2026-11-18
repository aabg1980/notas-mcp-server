# Notas MCP Server 🎓

> **Servidor MCP (Model Context Protocol)** desarrollado en **Java 25** con **Spring Boot 4.x** y **Spring AI 2.0.x**, con persistencia local en **SQLite**, diseñado para que asistentes y agentes de Inteligencia Artificial gestionen de forma autónoma y estructurada estudiantes, cursos, evaluaciones y calificaciones con cálculo automático de notas definitivas ponderadas.

---

## 📋 1. Definición del Proyecto

El **Notas MCP Server** actúa como un puente estandarizado mediante el protocolo abierto **Model Context Protocol (MCP)**, permitiendo a Modelos de Lenguaje (LLMs) como Claude, Gemini, ChatGPT o agentes autónomos interactuar con un sistema de gestión académica local de alto rendimiento.

### Objetivos Clave:
* **Autonomía para Asistentes IA:** Exponer herramientas fuertemente tipadas (`@Tool`) que permiten registrar estudiantes, definir cursos y pruebas académicas con pesos porcentuales, asentar notas y calcular promedios ponderados.
* **Persistencia Ligera y Confiable:** Uso de una base de datos local **SQLite**, garantizando portabilidad absoluta y cero dependencias de red o servicios externos.
* **Vanguardia Tecnológica y Código Limpio:** Aprovechar las innovaciones de **Java 25** (Enhanced Virtual Threads, Stream Gatherers, Records inmutables, Pattern Matching) y la arquitectura modular de **Spring Boot 4 / Spring Framework 7**, siguiendo estrictamente principios **SOLID**, **Clean Code** y **Clean Architecture (Puertos y Adaptadores)**.

---

## 🛠️ 2. Stack Tecnológico

| Componente | Tecnología | Versión | Justificación |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Java | 25 (LTS) | Virtual Threads de alta concurrencia, Stream Gatherers, Records inmutables y Pattern Matching. |
| **Framework Base** | Spring Boot | 4.0.x | Basado en Spring Framework 7 y Jakarta EE 11; arquitectura modular y soporte nativo Java 25. |
| **Integración IA/MCP** | Spring AI | 2.0.x | Soporte de MCP Server compatible con Spring Boot 4 (transporte STDIO local y SSE/HTTP). |
| **Base de Datos** | SQLite | 3.x | Motor SQL embebido local en archivo (`notas.db`), rápido, ligero y autónomo. |
| **Capa de Persistencia** | Spring Data JPA + Hibernate Community | 7.x | Abstracción ORM con dialecto comunitario oficial de SQLite compatible con Jakarta EE 11. |
| **Controlador JDBC** | `org.xerial:sqlite-jdbc` | 3.47.x | Driver JDBC nativo de alto rendimiento para SQLite. |
| **Gestor de Compilación**| Apache Maven + Maven Wrapper | 3.9.x (`mvnw`) | Builds reproducibles y portables entre entornos y CI/CD. |
| **Pruebas Unitarias** | JUnit 5 + AssertJ + Mockito | - | Verificación aislada de reglas de negocio y cálculo de notas. |

---

## 🏛️ 3. Arquitectura y Clean Code

El sistema implementa **Clean Architecture (Arquitectura Hexagonal / Puertos y Adaptadores)** garantizando que el dominio académico sea completamente agnóstico de frameworks, controladores MCP o bases de datos:

```
┌────────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE (Adaptadores)                      │
│                                                                        │
│   ┌─────────────────────┐                  ┌──────────────────────┐    │
│   │   MCP Server API    │                  │  SQLite Persistence  │    │
│   │   (@Tool Adapters)  │                  │  (Spring Data JPA)   │    │
│   └──────────┬──────────┘                  └──────────▲───────────┘    │
│              │                                        │                │
│              ▼                                        │                │
│   ┌───────────────────────────────────────────────────┴────────────┐   │
│   │                   APPLICATION (Casos de Uso)                   │   │
│   │                                                                │   │
│   │   • StudentService           • GradeCalculationService         │   │
│   │   • EvaluationService        • DTOs (Java 25 Records)          │   │
│   │                                                                │   │
│   │   ┌────────────────────────────────────────────────────────┐   │   │
│   │   │                    DOMAIN (Núcleo)                     │   │   │
│   │   │                                                        │   │   │
│   │   │   • Student, Course, Evaluation, Grade                 │   │   │
│   │   │   • Repositories (Puertos / Interfaces)                │   │   │
│   │   │   • Reglas de Negocio (0.0 a 5.0, sumatoria ≤ 100%)    │   │   │
│   │   └────────────────────────────────────────────────────────┘   │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Principios Clean Code aplicados:
1. **Single Responsibility (SRP):** Servicios separados por agregado de dominio (estudiantes, cursos, pruebas y calificaciones).
2. **Inmutabilidad y Tipado Fuerte:** DTOs y modelos de transferencia basados en Java 25 `record`.
3. **Validación Temprana:** Excepciones de dominio específicas (`StudentNotFoundException`, `InvalidWeightException`, `GradeOutOfRangeException`).
4. **Desacoplamiento Absoluto:** Las herramientas MCP (`@Tool`) son adaptadores de entrada delegando exclusivamente en casos de uso de la capa de aplicación.

---

## 📁 4. Scaffolding del Proyecto

Estructura de directorios y paquetes:

```text
notas-mcp-server/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/aabg/notas/
│   │   │   ├── domain/                         # Núcleo del Dominio
│   │   │   │   ├── model/                      # Entidades y Value Objects puros
│   │   │   │   │   ├── Student.java
│   │   │   │   │   ├── Course.java
│   │   │   │   │   ├── Evaluation.java
│   │   │   │   │   └── Grade.java
│   │   │   │   ├── repository/                 # Puertos de salida (interfaces)
│   │   │   │   │   ├── StudentRepository.java
│   │   │   │   │   ├── CourseRepository.java
│   │   │   │   │   ├── EvaluationRepository.java
│   │   │   │   │   └── GradeRepository.java
│   │   │   │   └── exception/                  # Excepciones de negocio
│   │   │   │       ├── DomainException.java
│   │   │   │       ├── EntityNotFoundException.java
│   │   │   │       └── BusinessRuleException.java
│   │   │   ├── application/                    # Casos de uso
│   │   │   │   ├── dto/                        # Java 25 Records inmutables
│   │   │   │   │   ├── StudentDto.java
│   │   │   │   │   ├── CourseDto.java
│   │   │   │   │   ├── EvaluationDto.java
│   │   │   │   │   ├── GradeDto.java
│   │   │   │   │   └── FinalGradeReportDto.java
│   │   │   │   └── service/                    # Orquestadores de negocio
│   │   │   │       ├── StudentService.java
│   │   │   │       ├── CourseService.java
│   │   │   │       ├── EvaluationService.java
│   │   │   │       └── GradeCalculationService.java
│   │   │   ├── infrastructure/                 # Adaptadores tecnológicos
│   │   │   │   ├── persistence/                # SQLite + Spring Data JPA
│   │   │   │   │   ├── entity/                 # Entidades JPA (Jakarta EE 11)
│   │   │   │   │   │   ├── StudentEntity.java
│   │   │   │   │   │   ├── CourseEntity.java
│   │   │   │   │   │   ├── EvaluationEntity.java
│   │   │   │   │   │   └── GradeEntity.java
│   │   │   │   │   ├── repository/             # Spring Data Interfaces
│   │   │   │   │   │   ├── JpaStudentRepository.java
│   │   │   │   │   │   ├── JpaCourseRepository.java
│   │   │   │   │   │   ├── JpaEvaluationRepository.java
│   │   │   │   │   │   └── JpaGradeRepository.java
│   │   │   │   │   └── adapter/                # Implementaciones de puertos
│   │   │   │   │       ├── StudentRepositoryAdapter.java
│   │   │   │   │       ├── CourseRepositoryAdapter.java
│   │   │   │   │       ├── EvaluationRepositoryAdapter.java
│   │   │   │   │       └── GradeRepositoryAdapter.java
│   │   │   │   └── mcp/                        # Servidor MCP y herramientas
│   │   │   │       ├── tool/                   # Herramientas @Tool expuestas al LLM
│   │   │   │       │   ├── StudentMcpTools.java
│   │   │   │       │   ├── CourseMcpTools.java
│   │   │   │       │   ├── EvaluationMcpTools.java
│   │   │   │       │   └── GradeMcpTools.java
│   │   │   │       └── config/                 # Configuración de MCP Server
│   │   │   │           └── McpServerConfig.java
│   │   │   └── NotasMcpApplication.java        # Punto de entrada Spring Boot 4
│   │   └── resources/
│   │       ├── application.yml                 # Configuración general y SQLite
│   │       ├── schema.sql                      # Inicialización DDL de SQLite
│   │       └── data.sql                        # Datos semilla opcionales
│   └── test/                                   # Pruebas automatizadas
│       └── java/com/aabg/notas/
│           ├── application/service/            # Pruebas unitarias de servicios
│           └── infrastructure/mcp/             # Pruebas de herramientas MCP
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 🗄️ 5. Diseño de Base de Datos (SQLite)

### Diagrama Entidad-Relación

```mermaid
erDiagram
    COURSES ||--o{ COURSE_ENROLLMENTS : "matricula"
    STUDENTS ||--o{ COURSE_ENROLLMENTS : "se inscribe"
    COURSES ||--o{ ASSESSMENT_CATEGORIES : "define rubros"
    ASSESSMENT_CATEGORIES ||--o{ EVALUATIONS : "contiene pruebas"
    EVALUATIONS ||--o{ RUBRIC_CRITERIA : "especifica rúbrica"
    STUDENTS ||--o{ GRADES : "recibe calificación"
    EVALUATIONS ||--o{ GRADES : "evalúa con nota"

    COURSES {
        INTEGER id PK "Autoincrement"
        VARCHAR code "Código asignatura (ej. MAT101)"
        VARCHAR name "Nombre de la asignatura"
        VARCHAR academic_period "Periodo académico (ej. 2026-1)"
        VARCHAR group_id "ID de grupo (ej. G01, G02)"
    }

    STUDENTS {
        INTEGER id PK "Autoincrement"
        VARCHAR code UK "Código único (ej. EST-001)"
        VARCHAR first_name "Nombres"
        VARCHAR last_name "Apellidos"
        VARCHAR email UK "Correo institucional"
        VARCHAR status "Estado (ACTIVE, INACTIVE)"
    }

    COURSE_ENROLLMENTS {
        INTEGER id PK "Autoincrement"
        INTEGER course_id FK "ID de la asignatura/grupo"
        INTEGER student_id FK "ID del estudiante"
        DATETIME enrolled_at "Fecha de matrícula"
    }

    ASSESSMENT_CATEGORIES {
        INTEGER id PK "Autoincrement"
        INTEGER course_id FK "ID de la asignatura"
        VARCHAR name "Nombre del rubro (Quices, Parciales)"
        VARCHAR description "Descripción"
        DECIMAL weight_percentage "Peso del rubro en la materia (0-100%)"
    }

    EVALUATIONS {
        INTEGER id PK "Autoincrement"
        INTEGER category_id FK "ID del rubro"
        VARCHAR name "Nombre de la prueba (Quiz 1, Parcial 1)"
        VARCHAR description "Descripción o temario"
        VARCHAR evaluation_type "Modalidad (INDIVIDUAL o GROUP)"
        DECIMAL weight_within_category "Peso dentro del rubro (0-100%)"
        DATE due_date "Fecha límite de entrega"
        TEXT rubric_description "Descripción general de la rúbrica"
    }

    RUBRIC_CRITERIA {
        INTEGER id PK "Autoincrement"
        INTEGER evaluation_id FK "ID de la prueba"
        VARCHAR name "Criterio (ej. Claridad, Código Limpio)"
        VARCHAR description "Descripción detallada del criterio"
        DECIMAL max_points "Puntaje máximo asignable"
        DECIMAL weight_percentage "Peso del criterio en la prueba"
    }

    GRADES {
        INTEGER id PK "Autoincrement"
        INTEGER student_id FK "ID del estudiante"
        INTEGER evaluation_id FK "ID de la evaluación"
        DECIMAL score "Calificación obtenida (0.0 a 5.0)"
        VARCHAR feedback "Retroalimentación u observaciones"
        DATETIME graded_at "Fecha y hora del registro"
    }
```

### Reglas e Integridad de la Base de Datos:
1. **Escala de Calificación:** La columna `score` tiene constraint `CHECK (score >= 0.0 AND score <= 5.0)`.
2. **Jerarquía Ponderada:** 
   - La suma de `weight_percentage` de los rubros de una asignatura no supera el `100.0%`.
   - Cada prueba tiene su `weight_within_category` relativo al rubro al que pertenece (ej. 50% y 50% para dos quices dentro del rubro 'Quices').
3. **Modalidad de Pruebas:** `evaluation_type` validado con `CHECK (evaluation_type IN ('INDIVIDUAL', 'GROUP'))`.
4. **Rúbricas de Evaluación:** Cada prueba cuenta con criterios estructurados en `rubric_criteria` con puntajes y pesos específicos.
5. **Matrícula y Unicidad:** 
   - `UNIQUE(course_id, student_id)` en matrículas.
   - `UNIQUE(student_id, evaluation_id)` para impedir calificaciones duplicadas por prueba para un mismo alumno.
6. **Claves Foráneas Activas:** SQLite configurado con `PRAGMA foreign_keys = ON;` y modo WAL activado para concurrencia segura.

---

## ⚙️ 6. Capacidades y Herramientas MCP (`@Tool`)

El servidor MCP registra las siguientes herramientas para el agente o asistente:

| Herramienta | Entrada | Salida | Descripción |
| :--- | :--- | :--- | :--- |
| `add_student` | `code`, `firstName`, `lastName`, `email` | `StudentDto` | Registra un nuevo estudiante en el sistema. |
| `list_students` | Ninguna | `List<StudentDto>` | Retorna todos los estudiantes registrados. |
| `get_student` | `code` | `StudentDto` | Busca un estudiante por su código único. |
| `create_course` | `code`, `name`, `academicPeriod` | `CourseDto` | Registra una nueva asignatura o curso. |
| `list_courses` | Ninguna | `List<CourseDto>` | Lista las asignaturas activas. |
| `create_evaluation` | `courseCode`, `name`, `description`, `weight` | `EvaluationDto` | Crea una prueba asociada a un curso con su porcentaje. |
| `list_evaluations` | `courseCode` | `List<EvaluationDto>` | Consulta las evaluaciones programadas de un curso. |
| `record_grade` | `studentCode`, `evaluationId`, `score`, `feedback` | `GradeDto` | Asienta una calificación para un estudiante en una prueba. |
| `get_student_grades`| `studentCode`, `courseCode` | `List<GradeDto>` | Consulta el historial de notas de un alumno en un curso. |
| `calculate_final_grade`| `studentCode`, `courseCode` | `FinalGradeReportDto` | **Calcula la nota definitiva ponderada**, porcentaje acumulado y estado (Aprobado/Reprobado). |
| `get_course_summary` | `courseCode` | `CourseSummaryDto` | Genera el reporte consolidado de definitivas de todos los estudiantes de un curso. |

---

## 🌿 7. Estrategia de Ramas (Git Workflow)

El repositorio sigue un modelo de dos ramas principales:

* **`main`**: Rama de producción y versiones estables probadas y funcionales.
* **`develop`**: Rama base de integración y desarrollo continuo donde se implementan los features y herramientas.

```mermaid
gitGraph
   commit id: "Initial commit"
   branch develop
   checkout develop
   commit id: "feat: scaffolding maven y spring ai"
   commit id: "feat: diseno y tablas sqlite"
   commit id: "feat: servicios de dominio y calculo"
   commit id: "feat: herramientas mcp (@Tool)"
   checkout main
   merge develop id: "release: v1.0.0"
```

---

## 🚀 8. Configuración e Instalación

### Requisitos Previos:
* **Java JDK 25** (LTS) instalado y configurado en `JAVA_HOME`.
* Git.

### Compilación:
```bash
./mvnw clean package -DskipTests
```

### Ejecución Local:
```bash
./mvnw spring-boot:run
```

### Configuración en Clientes MCP (ej. Claude Desktop / Cursor / Antigravity):
Agregar en el archivo de configuración de clientes MCP (`claude_desktop_config.json` o similar):

```json
{
  "mcpServers": {
    "notas-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/ruta/hacia/notas-mcp-server/target/notas-mcp-server-1.0.0.jar"
      ]
    }
  }
}
```

---

**Autor:** [Andrés Barrera Garavito](https://github.com/aabg1980)  
**Licencia:** Apache 2.0

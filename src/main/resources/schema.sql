-- ============================================================================
-- Esquema de Base de Datos SQLite para Notas MCP Server
-- ============================================================================

PRAGMA foreign_keys = ON;

-- 1. Tabla de Estudiantes
CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Asignaturas / Grupos por Periodo Académico
CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    academic_period TEXT NOT NULL,
    group_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN' CHECK(status IN ('OPEN', 'CLOSED')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_course_period_group UNIQUE (code, academic_period, group_id)
);

-- 3. Matrícula de Estudiantes en Cursos (Relación Muchos a Muchos)
CREATE TABLE IF NOT EXISTS course_enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_course_student UNIQUE (course_id, student_id)
);

-- 4. Rubros de Evaluación de una Asignatura (ej. Quices 20%, Talleres 20%, Parcial 1 30%, Final 30%)
CREATE TABLE IF NOT EXISTS assessment_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    weight_percentage REAL NOT NULL CHECK(weight_percentage > 0.0 AND weight_percentage <= 100.0),
    CONSTRAINT uq_course_category UNIQUE (course_id, name)
);

-- 5. Pruebas / Evaluaciones pertenecientes a un Rubro (ej. Quiz 1, Quiz 2 dentro de Quices)
CREATE TABLE IF NOT EXISTS evaluations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL REFERENCES assessment_categories(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    evaluation_type TEXT NOT NULL DEFAULT 'INDIVIDUAL' CHECK(evaluation_type IN ('INDIVIDUAL', 'GROUP')),
    weight_within_category REAL NOT NULL DEFAULT 100.0 CHECK(weight_within_category > 0.0 AND weight_within_category <= 100.0),
    due_date DATE,
    rubric_description TEXT,
    CONSTRAINT uq_category_evaluation UNIQUE (category_id, name)
);

-- 6. Criterios de la Rúbrica de una Prueba
CREATE TABLE IF NOT EXISTS rubric_criteria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evaluation_id INTEGER NOT NULL REFERENCES evaluations(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    max_points REAL NOT NULL CHECK(max_points > 0.0),
    weight_percentage REAL NOT NULL CHECK(weight_percentage > 0.0 AND weight_percentage <= 100.0)
);

-- 7. Calificaciones de Estudiantes por Prueba (Escala 0.0 a 5.0)
CREATE TABLE IF NOT EXISTS grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    evaluation_id INTEGER NOT NULL REFERENCES evaluations(id) ON DELETE CASCADE,
    score REAL NOT NULL CHECK(score >= 0.0 AND score <= 5.0),
    feedback TEXT,
    graded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_evaluation_grade UNIQUE (student_id, evaluation_id)
);

-- 8. Puntuaciones por Criterio de Rúbrica (Desglose analítico de la nota)
CREATE TABLE IF NOT EXISTS grade_rubric_scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    grade_id INTEGER NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
    criterion_id INTEGER NOT NULL REFERENCES rubric_criteria(id) ON DELETE CASCADE,
    score_obtained REAL NOT NULL CHECK(score_obtained >= 0.0),
    feedback TEXT,
    CONSTRAINT uq_grade_criterion UNIQUE (grade_id, criterion_id)
);

-- Índices para optimización de consultas
CREATE INDEX IF NOT EXISTS idx_courses_period_group ON courses(academic_period, group_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON course_enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_categories_course ON assessment_categories(course_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_category ON evaluations(category_id);
CREATE INDEX IF NOT EXISTS idx_grades_student ON grades(student_id);
CREATE INDEX IF NOT EXISTS idx_grades_evaluation ON grades(evaluation_id);
CREATE INDEX IF NOT EXISTS idx_rubric_evaluation ON rubric_criteria(evaluation_id);

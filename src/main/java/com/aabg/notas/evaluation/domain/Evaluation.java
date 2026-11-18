package com.aabg.notas.evaluation.domain;

import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregado raíz del dominio académico que representa una prueba o evaluación
 * específica dentro de un rubro de una asignatura, junto con su rúbrica y las
 * notas (calificaciones) registradas para los estudiantes.
 *
 * <p>Sigue el patrón Rich Domain Model: la raíz protege sus invariantes y expone
 * comportamiento, no setters. Las invariantes del agregado son:</p>
 * <ul>
 *   <li>La evaluación pertenece obligatoriamente a un rubro (referencia por ID al agregado Course).</li>
 *   <li>El nombre es obligatorio y el peso dentro del rubro es un {@link WeightPercentage} válido.</li>
 *   <li>La sumatoria de pesos de los criterios de la rúbrica nunca excede el 100%.</li>
 *   <li>Los nombres de criterios son únicos dentro de la evaluación.</li>
 *   <li>Cada estudiante tiene a lo sumo una nota en la evaluación: registrar una nota
 *       para un estudiante ya calificado actualiza la existente (upsert).</li>
 *   <li>Las notas son {@link GradeScore} en la escala 0.0 a 5.0, siempre válidas por construcción.</li>
 *   <li>Las colecciones de criterios y notas están protegidas: solo se mutan vía la raíz.</li>
 * </ul>
 *
 * <p>La igualdad de identidad se define por la tupla (rubro, nombre).</p>
 */
public class Evaluation {

    /** Límite superior de la sumatoria de pesos de los criterios de la rúbrica. */
    private static final BigDecimal RUBRIC_WEIGHT_LIMIT = WeightPercentage.MAX;

    private final Long id;
    private final Long categoryId;
    private String name;
    private final String description;
    private final EvaluationType type;
    private WeightPercentage weightWithinCategory;
    private final LocalDate dueDate;
    private final String rubricDescription;
    private final List<RubricCriterion> criteria = new ArrayList<>();
    private final List<Grade> grades = new ArrayList<>();

    private Evaluation(Long id, Long categoryId, String name, String description, EvaluationType type,
                       WeightPercentage weightWithinCategory, LocalDate dueDate, String rubricDescription,
                       List<RubricCriterion> criteria, List<Grade> grades) {
        validateCategoryId(categoryId);
        validateName(name);

        this.id = id;
        this.categoryId = categoryId;
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.type = type != null ? type : EvaluationType.INDIVIDUAL;
        this.weightWithinCategory = Objects.requireNonNull(weightWithinCategory,
                "El peso de la evaluación dentro del rubro es obligatorio");
        this.dueDate = dueDate;
        this.rubricDescription = rubricDescription == null ? "" : rubricDescription.trim();
        if (criteria != null) {
            this.criteria.addAll(criteria);
        }
        if (grades != null) {
            this.grades.addAll(grades);
        }
    }

    /** Factory method para crear una evaluación nueva sin criterios ni notas. */
    public static Evaluation create(Long categoryId, String name, String description, EvaluationType type,
                                    WeightPercentage weightWithinCategory, LocalDate dueDate,
                                    String rubricDescription) {
        return new Evaluation(null, categoryId, name, description, type, weightWithinCategory,
                dueDate, rubricDescription, null, null);
    }

    /** Factory method para reconstruir el agregado completo desde la persistencia. */
    public static Evaluation rehydrate(Long id, Long categoryId, String name, String description,
                                       EvaluationType type, WeightPercentage weightWithinCategory,
                                       LocalDate dueDate, String rubricDescription,
                                       List<RubricCriterion> criteria, List<Grade> grades) {
        return new Evaluation(id, categoryId, name, description, type, weightWithinCategory,
                dueDate, rubricDescription, criteria, grades);
    }

    // ==================== Comportamiento del agregado ====================

    /** Renombra la evaluación manteniendo su identidad. */
    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
    }

    /** Actualiza el peso de la evaluación dentro de su rubro. */
    public void updateWeight(WeightPercentage newWeight) {
        Objects.requireNonNull(newWeight, "El nuevo peso de la evaluación es obligatorio");
        this.weightWithinCategory = newWeight;
    }

    /**
     * Agrega un criterio a la rúbrica de la evaluación.
     *
     * <p>Invariantes verificadas antes de mutar el estado:</p>
     * <ol>
     *   <li>El nombre del criterio no puede repetirse dentro de la evaluación.</li>
     *   <li>La sumatoria de pesos de la rúbrica con el nuevo criterio no puede exceder el 100%.</li>
     * </ol>
     *
     * @return el criterio creado (aún sin identidad persistida)
     */
    public RubricCriterion addCriterion(String name, String description, MaxPoints maxPoints,
                                        WeightPercentage weight) {
        Objects.requireNonNull(maxPoints, "El puntaje máximo del criterio es obligatorio");
        Objects.requireNonNull(weight, "El peso del criterio es obligatorio");

        if (hasCriterionNamed(name)) {
            throw new BusinessRuleException(String.format(
                    "La evaluación '%s' ya tiene un criterio de rúbrica llamado '%s'", this.name, name.trim()));
        }

        BigDecimal newTotal = totalRubricWeight().add(weight.value());
        if (newTotal.compareTo(RUBRIC_WEIGHT_LIMIT) > 0) {
            throw new BusinessRuleException(String.format(
                    "No es posible agregar el criterio '%s' con peso %s: la sumatoria de pesos de la rúbrica "
                            + "de la evaluación '%s' excedería el 100%% (total sería %s)",
                    name, weight, this.name,
                    newTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        }

        RubricCriterion criterion = RubricCriterion.create(name, description, maxPoints, weight);
        criteria.add(criterion);
        return criterion;
    }

    /** Elimina un criterio existente de la rúbrica. */
    public void removeCriterion(Long criterionId) {
        RubricCriterion criterion = getCriterionById(criterionId);
        criteria.remove(criterion);
    }

    /**
     * Registra (o actualiza) la nota de un estudiante en esta evaluación.
     *
     * <p>Regla de negocio: un estudiante tiene una única nota por evaluación. Si ya
     * existe una nota para el estudiante, se actualiza su calificación y retroalimentación
     * (upsert); si no existe, se crea una nueva. El rango válido 0.0 a 5.0 está garantizado
     * por el Value Object {@link GradeScore}.</p>
     *
     * @return la nota registrada o actualizada
     */
    public Grade recordGrade(Long studentId, GradeScore score, String feedback) {
        Objects.requireNonNull(score, "La calificación es obligatoria");

        Optional<Grade> existing = gradeForStudent(studentId);
        if (existing.isPresent()) {
            existing.get().updateScore(score, feedback);
            return existing.get();
        }

        Grade grade = Grade.create(Objects.requireNonNull(studentId, "El estudiante es obligatorio"), score, feedback);
        grades.add(grade);
        return grade;
    }

    /** Elimina la nota de un estudiante en esta evaluación, si existe. */
    public void removeGrade(Long studentId) {
        Grade grade = gradeForStudent(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Calificación", String.valueOf(studentId)));
        grades.remove(grade);
    }

    /**
     * Promedio aritmético de las notas registradas en la evaluación, con escala de 2 decimales.
     * Vacío si la evaluación aún no tiene notas.
     */
    public Optional<GradeScore> averageScore() {
        if (grades.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal sum = grades.stream()
                .map(g -> g.getScore().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
        return Optional.of(new GradeScore(average));
    }

    /** Indica cuántas de las notas registradas aprueban (>= 3.0). */
    public long passingGradeCount() {
        return grades.stream().filter(Grade::isPassing).count();
    }

    /** Busca la nota de un estudiante dentro de la evaluación. */
    public Optional<Grade> gradeForStudent(Long studentId) {
        return grades.stream()
                .filter(g -> Objects.equals(g.getStudentId(), studentId))
                .findFirst();
    }

    /** Busca un criterio por su identidad dentro del agregado. */
    public Optional<RubricCriterion> findCriterionById(Long criterionId) {
        return criteria.stream()
                .filter(c -> Objects.equals(c.getId(), criterionId))
                .findFirst();
    }

    /** Sumatoria actual de los pesos de la rúbrica, con escala de 2 decimales. */
    public BigDecimal totalRubricWeight() {
        return criteria.stream()
                .map(c -> c.getWeight().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== Consultas ====================

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EvaluationType getType() {
        return type;
    }

    public WeightPercentage getWeightWithinCategory() {
        return weightWithinCategory;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getRubricDescription() {
        return rubricDescription;
    }

    /** Vista de solo lectura de los criterios de la rúbrica. */
    public List<RubricCriterion> getCriteria() {
        return List.copyOf(criteria);
    }

    /** Vista de solo lectura de las notas registradas. */
    public List<Grade> getGrades() {
        return List.copyOf(grades);
    }

    // ==================== Soporte interno ====================

    private RubricCriterion getCriterionById(Long criterionId) {
        return findCriterionById(criterionId)
                .orElseThrow(() -> new EntityNotFoundException("Criterio de rúbrica",
                        String.valueOf(criterionId)));
    }

    private boolean hasCriterionNamed(String candidateName) {
        return criteria.stream().anyMatch(c -> c.hasName(candidateName));
    }

    private static void validateCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessRuleException("La evaluación debe pertenecer a un rubro de evaluación de una asignatura");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre de la evaluación no puede estar vacío");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Evaluation that)) {
            return false;
        }
        return Objects.equals(categoryId, that.categoryId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, name);
    }
}

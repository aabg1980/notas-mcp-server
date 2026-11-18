package com.aabg.notas.course.domain;

import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregado raíz del dominio académico que representa una asignatura impartida
 * en un periodo académico y grupo específico.
 *
 * <p>Sigue el patrón Rich Domain Model: la raíz protege sus invariantes y expone
 * comportamiento, no setters. Las invariantes del agregado son:</p>
 * <ul>
 *   <li>El código, nombre, periodo académico y grupo son obligatorios.</li>
 *   <li>La sumatoria de pesos de los rubros nunca excede el 100%.</li>
 *   <li>Los nombres de rubros son únicos dentro de la asignatura (sin distinción de mayúsculas).</li>
 *   <li>Una asignatura cerrada (CLOSED) no admite modificaciones en su estructura de rubros.</li>
 *   <li>Las colecciones de rubros están protegidas: solo se mutan a través de métodos de la raíz.</li>
 * </ul>
 *
 * <p>La igualdad de identidad se define por la tupla (código, periodo académico, grupo).</p>
 */
public class Course {

    /** Límite superior de la sumatoria de pesos de los rubros. */
    private static final BigDecimal TOTAL_WEIGHT_LIMIT = WeightPercentage.MAX;

    private final Long id;
    private final String code;
    private String name;
    private final String academicPeriod;
    private final String groupId;
    private CourseStatus status;
    private final List<AssessmentCategory> categories = new ArrayList<>();

    private Course(Long id, String code, String name, String academicPeriod, String groupId,
                   CourseStatus status, List<AssessmentCategory> categories) {
        validateCode(code);
        validateName(name);
        validateAcademicPeriod(academicPeriod);
        validateGroupId(groupId);

        this.id = id;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.academicPeriod = academicPeriod.trim();
        this.groupId = groupId.trim().toUpperCase();
        this.status = status != null ? status : CourseStatus.OPEN;
        if (categories != null) {
            this.categories.addAll(categories);
        }
    }

    /** Factory method para crear una asignatura nueva con estado OPEN y sin rubros. */
    public static Course create(String code, String name, String academicPeriod, String groupId) {
        return new Course(null, code, name, academicPeriod, groupId, CourseStatus.OPEN, null);
    }

    /** Factory method para reconstruir el agregado completo desde la persistencia. */
    public static Course rehydrate(Long id, String code, String name, String academicPeriod, String groupId,
                                   CourseStatus status, List<AssessmentCategory> categories) {
        return new Course(id, code, name, academicPeriod, groupId, status, categories);
    }

    // ==================== Comportamiento del agregado ====================

    /** Renombra la asignatura. Operación permitida incluso con el curso cerrado. */
    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
    }

    /**
     * Agrega un rubro de evaluación a la asignatura.
     *
     * <p>Invariantes verificadas antes de mutar el estado:</p>
     * <ol>
     *   <li>La asignatura debe estar abierta (OPEN).</li>
     *   <li>El nombre del rubro no puede repetirse dentro de la asignatura.</li>
     *   <li>La sumatoria de pesos de rubros con el nuevo peso no puede exceder el 100%.</li>
     * </ol>
     *
     * @return el rubro creado (aún sin identidad persistida; el adaptador de persistencia le asignará id)
     */
    public AssessmentCategory addCategory(String name, String description, WeightPercentage weight) {
        ensureOpen("agregar rubros");
        Objects.requireNonNull(weight, "El peso del rubro es obligatorio");

        if (hasCategoryNamed(name)) {
            throw new BusinessRuleException(String.format(
                    "La asignatura '%s' ya tiene un rubro llamado '%s'", code, name.trim()));
        }

        BigDecimal newTotal = totalWeight().add(weight.value());
        if (newTotal.compareTo(TOTAL_WEIGHT_LIMIT) > 0) {
            throw new BusinessRuleException(String.format(
                    "No es posible agregar el rubro '%s' con peso %s: la sumatoria de pesos de la "
                            + "asignatura '%s' excedería el 100%% (total sería %s)",
                    name, weight, code, newTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        }

        AssessmentCategory category = AssessmentCategory.create(name, description, weight);
        categories.add(category);
        return category;
    }

    /**
     * Actualiza el peso de un rubro existente, verificando que la sumatoria total
     * (excluyendo el peso anterior del rubro) no exceda el 100%.
     */
    public void updateCategoryWeight(Long categoryId, WeightPercentage newWeight) {
        ensureOpen("modificar pesos de rubros");
        Objects.requireNonNull(newWeight, "El nuevo peso del rubro es obligatorio");

        AssessmentCategory category = getCategoryById(categoryId);
        BigDecimal otherWeights = categories.stream()
                .filter(c -> !c.getId().equals(categoryId))
                .map(c -> c.getWeight().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = otherWeights.add(newWeight.value());
        if (newTotal.compareTo(TOTAL_WEIGHT_LIMIT) > 0) {
            throw new BusinessRuleException(String.format(
                    "No es posible actualizar el rubro '%s' a un peso de %s: la sumatoria de pesos de la "
                            + "asignatura '%s' excedería el 100%% (total sería %s)",
                    category.getName(), newWeight, code,
                    newTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        }

        category.updateWeight(newWeight);
    }

    /** Elimina un rubro existente de la asignatura. Solo permitido con la asignatura abierta. */
    public void removeCategory(Long categoryId) {
        ensureOpen("eliminar rubros");
        AssessmentCategory category = getCategoryById(categoryId);
        categories.remove(category);
    }

    /** Cierra la asignatura: a partir de ahora no admite cambios en su estructura de rubros. */
    public void close() {
        if (status == CourseStatus.CLOSED) {
            throw new BusinessRuleException(String.format("La asignatura '%s' ya se encuentra cerrada", code));
        }
        this.status = CourseStatus.CLOSED;
    }

    /** Reabre una asignatura cerrada para volver a modificar su estructura. */
    public void reopen() {
        if (status == CourseStatus.OPEN) {
            throw new BusinessRuleException(String.format("La asignatura '%s' ya se encuentra abierta", code));
        }
        this.status = CourseStatus.OPEN;
    }

    /** Indica si la asignatura admite modificaciones en su estructura de rubros. */
    public boolean isOpen() {
        return status == CourseStatus.OPEN;
    }

    /**
     * Sumatoria actual de los pesos de todos los rubros, con escala de 2 decimales.
     * Nunca puede exceder 100.00 gracias a las invariantes del agregado.
     */
    public BigDecimal totalWeight() {
        return categories.stream()
                .map(c -> c.getWeight().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Indica si la asignatura ya tiene su distribución de pesos completa (total = 100%). */
    public boolean hasCompleteWeightDistribution() {
        return totalWeight().compareTo(TOTAL_WEIGHT_LIMIT) == 0;
    }

    /** Busca un rubro por su identidad dentro del agregado. */
    public Optional<AssessmentCategory> findCategoryById(Long categoryId) {
        return categories.stream()
                .filter(c -> Objects.equals(c.getId(), categoryId))
                .findFirst();
    }

    /** Clave única de negocio de la asignatura (código + periodo + grupo). */
    public String getUniqueKey() {
        return code + "-" + academicPeriod + "-" + groupId;
    }

    // ==================== Consultas ====================

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAcademicPeriod() {
        return academicPeriod;
    }

    public String getGroupId() {
        return groupId;
    }

    public CourseStatus getStatus() {
        return status;
    }

    /** Vista de solo lectura de los rubros. La mutación solo es posible vía la raíz del agregado. */
    public List<AssessmentCategory> getCategories() {
        return List.copyOf(categories);
    }

    // ==================== Soporte interno ====================

    private AssessmentCategory getCategoryById(Long categoryId) {
        return findCategoryById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Rubro de evaluación",
                        String.valueOf(categoryId)));
    }

    private boolean hasCategoryNamed(String candidateName) {
        return categories.stream().anyMatch(c -> c.hasName(candidateName));
    }

    private void ensureOpen(String operation) {
        if (!isOpen()) {
            throw new BusinessRuleException(String.format(
                    "La asignatura '%s' está cerrada y no permite %s", code, operation));
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El código de la asignatura no puede estar vacío");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre de la asignatura no puede estar vacío");
        }
    }

    private static void validateAcademicPeriod(String academicPeriod) {
        if (academicPeriod == null || academicPeriod.isBlank()) {
            throw new BusinessRuleException("El periodo académico no puede estar vacío (ej. '2026-1')");
        }
    }

    private static void validateGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            throw new BusinessRuleException("El ID de grupo no puede estar vacío (ej. 'G01')");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course course)) {
            return false;
        }
        return Objects.equals(code, course.code)
                && Objects.equals(academicPeriod, course.academicPeriod)
                && Objects.equals(groupId, course.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, academicPeriod, groupId);
    }
}

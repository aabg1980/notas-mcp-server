package com.aabg.notas.course.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aabg.notas.shared.domain.WeightPercentage;
import com.aabg.notas.shared.exception.BusinessRuleException;
import com.aabg.notas.shared.exception.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las invariantes del agregado Course (modelo rico).
 * Verifican que la raíz protege sus reglas de negocio sin servicios externos.
 */
class CourseTest {

    private static final WeightPercentage DIEZ = WeightPercentage.of("10.00");
    private static final WeightPercentage CINCUENTA = WeightPercentage.of("50.00");

    @Test
    @DisplayName("create: crea una asignatura abierta y normaliza código y grupo")
    void createNormalizesCodeAndGroupId() {
        Course course = Course.create("mat101", "Matemáticas I", "2027-1", "g01");

        assertThat(course.getId()).isNull();
        assertThat(course.getCode().value()).isEqualTo("MAT101");
        assertThat(course.getGroupId().value()).isEqualTo("G01");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
        assertThat(course.getCategories()).isEmpty();
        assertThat(course.totalWeight()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("create: rechaza datos obligatorios vacíos")
    void createRejectsBlankFields() {
        assertThatThrownBy(() -> Course.create("", "Matemáticas I", "2027-1", "G01"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("código");
        assertThatThrownBy(() -> Course.create("MAT101", "  ", "2027-1", "G01"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> Course.create("MAT101", "Matemáticas I", "", "G01"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("periodo académico");
        assertThatThrownBy(() -> Course.create("MAT101", "Matemáticas I", "2027-1", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("grupo");
    }

    @Test
    @DisplayName("AcademicPeriod: catálogo finito de periodos válidos (enum)")
    void academicPeriodIsAFiniteCatalog() {
        assertThat(AcademicPeriod.from("2027-1")).isEqualTo(AcademicPeriod.TERM_2027_1);
        assertThat(AcademicPeriod.from(" 2027-3 ")).isEqualTo(AcademicPeriod.TERM_2027_3);
        assertThat(AcademicPeriod.TERM_2027_1.value()).isEqualTo("2027-1");
        assertThat(AcademicPeriod.TERM_2027_3.year()).isEqualTo("2027");
        assertThat(AcademicPeriod.TERM_2027_3.term()).isEqualTo("3");

        assertThatThrownBy(() -> AcademicPeriod.from("2026-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no es válido");
        assertThatThrownBy(() -> Course.create("MAT101", "Matemáticas I", "2026-2", "G01"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("addCategory: agrega rubros acumulando la sumatoria de pesos")
    void addCategoryAccumulatesWeights() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");

        course.addCategory("Quices", "Quices de la asignatura", CINCUENTA);
        course.addCategory("Talleres", null, CINCUENTA);

        assertThat(course.getCategories()).hasSize(2);
        assertThat(course.totalWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(course.hasCompleteWeightDistribution()).isTrue();
    }

    @Test
    @DisplayName("addCategory: rechaza la sumatoria de pesos que exceda el 100%")
    void addCategoryRejectsWeightsExceeding100() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        course.addCategory("Quices", null, CINCUENTA);

        assertThatThrownBy(() -> course.addCategory("Parciales", null, CINCUENTA.add(DIEZ)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("excedería el 100%");

        // El estado no se mutó: la validación ocurre antes de agregar
        assertThat(course.getCategories()).hasSize(1);
        assertThat(course.totalWeight()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("addCategory: rechaza nombres de rubro duplicados (case-insensitive)")
    void addCategoryRejectsDuplicateNames() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        course.addCategory("Quices", null, DIEZ);

        assertThatThrownBy(() -> course.addCategory("quices", null, DIEZ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene un rubro");
    }

    @Test
    @DisplayName("updateCategoryWeight: valida la sumatoria excluyendo el peso anterior del rubro")
    void updateCategoryWeightConsidersPreviousWeight() {
        Course course = persistedCourse(CINCUENTA, CINCUENTA);

        // Excluir el 50% anterior de Quices deja 50% (Talleres); 40% no excede el límite
        course.updateCategoryWeight(10L, WeightPercentage.of("40.00"));
        assertThat(course.findCategoryById(10L).orElseThrow().getWeight())
                .isEqualTo(WeightPercentage.of("40.00"));

        // Subir Quices a 55% excede: 50% (Talleres) + 55% = 105%
        assertThatThrownBy(() -> course.updateCategoryWeight(10L, WeightPercentage.of("55.00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("excedería el 100%");
    }

    @Test
    @DisplayName("updateCategoryWeight: rechaza un rubro inexistente")
    void updateCategoryWeightRejectsUnknownCategory() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");

        assertThatThrownBy(() -> course.updateCategoryWeight(999L, DIEZ))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("removeCategory: elimina el rubro y libera su peso")
    void removeCategoryFreesWeight() {
        Course course = persistedCourse(CINCUENTA, CINCUENTA);

        course.removeCategory(10L);

        assertThat(course.getCategories()).hasSize(1);
        assertThat(course.totalWeight()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("removeCategory: rechaza un rubro inexistente")
    void removeCategoryRejectsUnknownCategory() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");

        assertThatThrownBy(() -> course.removeCategory(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("close: impide modificar rubros de una asignatura cerrada")
    void closedCourseRejectsCategoryChanges() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        course.close();

        assertThat(course.isOpen()).isFalse();
        assertThatThrownBy(() -> course.addCategory("Quices", null, DIEZ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cerrada");
        assertThatThrownBy(() -> course.close())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya se encuentra cerrada");
    }

    @Test
    @DisplayName("reopen: reabre una asignatura cerrada")
    void reopenAllowsModificationsAgain() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        course.close();
        course.reopen();

        assertThat(course.isOpen()).isTrue();
        assertThat(course.addCategory("Quices", null, DIEZ)).isNotNull();
    }

    @Test
    @DisplayName("getCategories: la colección es de solo lectura para el exterior")
    void categoriesCollectionIsReadOnly() {
        Course course = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        course.addCategory("Quices", null, DIEZ);

        assertThatThrownBy(() -> course.getCategories().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("igualdad de identidad por (código, periodo, grupo)")
    void identityIsCodePeriodGroup() {
        Course first = Course.create("MAT101", "Matemáticas I", "2027-1", "G01");
        Course second = Course.create("MAT101", "Otro nombre", "2027-1", "G01");
        Course other = Course.create("MAT101", "Matemáticas I", "2027-1", "G02");

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(other);
        assertThat(first.getUniqueKey()).isEqualTo("MAT101-2027-1-G01");
    }

    /**
     * Construye una asignatura con dos rubros con identidad (simula el agregado
     * reconstruido desde la persistencia): Quices con {@code quicesWeight} y
     * Talleres con {@code talleresWeight}.
     */
    private Course persistedCourse(WeightPercentage quicesWeight, WeightPercentage talleresWeight) {
        return Course.rehydrate(
                1L, "MAT101", "Matemáticas I", "2027-1", "G01", CourseStatus.OPEN,
                List.of(
                        AssessmentCategory.rehydrate(10L, "Quices", "", quicesWeight),
                        AssessmentCategory.rehydrate(11L, "Talleres", "", talleresWeight)));
    }
}

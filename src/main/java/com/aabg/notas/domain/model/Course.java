package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Entidad de dominio pura que representa una asignatura impartida en un periodo académico y grupo.
 */
public class Course {

    private final Long id;
    private final String code;
    private final String name;
    private final String academicPeriod;
    private final String groupId;

    public Course(Long id, String code, String name, String academicPeriod, String groupId) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El código de la asignatura no puede estar vacío");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre de la asignatura no puede estar vacío");
        }
        if (academicPeriod == null || academicPeriod.isBlank()) {
            throw new BusinessRuleException("El periodo académico no puede estar vacío (ej. '2026-1')");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new BusinessRuleException("El ID de grupo no puede estar vacío (ej. 'G01')");
        }

        this.id = id;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.academicPeriod = academicPeriod.trim();
        this.groupId = groupId.trim().toUpperCase();
    }

    public static Course createNew(String code, String name, String academicPeriod, String groupId) {
        return new Course(null, code, name, academicPeriod, groupId);
    }

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

    public String getUniqueKey() {
        return code + "-" + academicPeriod + "-" + groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return Objects.equals(code, course.code) &&
               Objects.equals(academicPeriod, course.academicPeriod) &&
               Objects.equals(groupId, course.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, academicPeriod, groupId);
    }
}

package com.aabg.notas.domain.model;

import com.aabg.notas.domain.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Entidad de dominio pura que representa a un estudiante.
 */
public class Student {

    private final Long id;
    private final String code;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final StudentStatus status;

    public Student(Long id, String code, String firstName, String lastName, String email, StudentStatus status) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El código del estudiante no puede estar vacío");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessRuleException("El nombre del estudiante no puede estar vacío");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessRuleException("El apellido del estudiante no puede estar vacío");
        }
        if (email == null || !email.contains("@")) {
            throw new BusinessRuleException("El correo electrónico del estudiante no es válido");
        }

        this.id = id;
        this.code = code.trim().toUpperCase();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
        this.status = status != null ? status : StudentStatus.ACTIVE;
    }

    public static Student createNew(String code, String firstName, String lastName, String email) {
        return new Student(null, code, firstName, lastName, email, StudentStatus.ACTIVE);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public StudentStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return Objects.equals(code, student.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}

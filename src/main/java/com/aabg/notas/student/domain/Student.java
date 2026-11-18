package com.aabg.notas.student.domain;

import com.aabg.notas.shared.exception.BusinessRuleException;
import java.util.Objects;

/**
 * Rich Domain Model que representa a un estudiante.
 */
public class Student {

    private final Long id;
    private final String code;
    private String firstName;
    private String lastName;
    private String email;
    private StudentStatus status;

    private Student(Long id, String code, String firstName, String lastName, String email, StudentStatus status) {
        validateCode(code);
        validateName(firstName, "nombre");
        validateName(lastName, "apellido");
        validateEmail(email);

        this.id = id;
        this.code = code.trim().toUpperCase();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
        this.status = status != null ? status : StudentStatus.ACTIVE;
    }

    public static Student create(String code, String firstName, String lastName, String email) {
        return new Student(null, code, firstName, lastName, email, StudentStatus.ACTIVE);
    }

    public static Student rehydrate(Long id, String code, String firstName, String lastName, String email, StudentStatus status) {
        return new Student(id, code, firstName, lastName, email, status);
    }

    public void updatePersonalInformation(String newFirstName, String newLastName, String newEmail) {
        validateName(newFirstName, "nombre");
        validateName(newLastName, "apellido");
        validateEmail(newEmail);

        this.firstName = newFirstName.trim();
        this.lastName = newLastName.trim();
        this.email = newEmail.trim().toLowerCase();
    }

    public void deactivate() {
        if (this.status == StudentStatus.INACTIVE) {
            throw new BusinessRuleException(String.format("El estudiante '%s' ya se encuentra inactivo", this.code));
        }
        this.status = StudentStatus.INACTIVE;
    }

    public void activate() {
        if (this.status == StudentStatus.ACTIVE) {
            throw new BusinessRuleException(String.format("El estudiante '%s' ya se encuentra activo", this.code));
        }
        this.status = StudentStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == StudentStatus.ACTIVE;
    }

    public void validateCanBeEvaluated() {
        if (!isActive()) {
            throw new BusinessRuleException(String.format("El estudiante '%s' está inactivo y no puede recibir calificaciones ni matricularse", this.code));
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El código del estudiante es obligatorio");
        }
    }

    private static void validateName(String name, String field) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(String.format("El %s del estudiante es obligatorio", field));
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessRuleException("El correo electrónico del estudiante no tiene un formato válido");
        }
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

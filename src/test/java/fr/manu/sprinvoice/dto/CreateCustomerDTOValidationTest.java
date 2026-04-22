package fr.manu.sprinvoice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TYPE : Test unitaire de validation Bean Validation (JUnit 5 pur)
 *
 * Vérifie que les contraintes @Pattern et @NotBlank sur CreateCustomerDTO
 * acceptent les mots de passe valides et rejettent les mots de passe faibles.
 *
 * Règle : 8 caractères minimum, 1 majuscule, 1 chiffre, 1 caractère spécial.
 */
class CreateCustomerDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<ConstraintViolation<CreateCustomerDTO>> validate(String username, String password) {
        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return validator.validate(dto);
    }

    /**
     * Un mot de passe valide ne doit produire aucune violation.
     * "Admin123!" : 9 chars, 1 maj, 1 chiffre, 1 spécial → OK
     */
    @Test
    void password_valid_passesValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "Admin123!");
        assertThat(violations).isEmpty();
    }

    /**
     * Un mot de passe trop court (< 8 chars) doit être refusé.
     * "Ab1!" : 4 caractères → KO
     */
    @Test
    void password_tooShort_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "Ab1!");
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    /**
     * Un mot de passe sans majuscule doit être refusé.
     * "abcdef1!" : 8 chars, pas de majuscule → KO
     */
    @Test
    void password_noUppercase_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "abcdef1!");
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    /**
     * Un mot de passe sans chiffre doit être refusé.
     * "Abcdefg!" : 8 chars, 1 maj, 1 spécial, pas de chiffre → KO
     */
    @Test
    void password_noDigit_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "Abcdefg!");
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    /**
     * Un mot de passe sans caractère spécial doit être refusé.
     * "Abcdef12" : 8 chars, 1 maj, 1 chiffre, pas de spécial → KO
     */
    @Test
    void password_noSpecialChar_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "Abcdef12");
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    /**
     * Un nom d'utilisateur vide doit être refusé (@NotBlank).
     */
    @Test
    void username_blank_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("", "Admin123!");
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    /**
     * Un mot de passe vide doit être refusé (@NotBlank).
     */
    @Test
    void password_blank_failsValidation() {
        Set<ConstraintViolation<CreateCustomerDTO>> violations = validate("utilisateur", "");
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}

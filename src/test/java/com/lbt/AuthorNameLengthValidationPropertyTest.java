package com.lbt;

import com.lbt.entities.Author;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 1: Name length validation
 *
 * For any string > 150 chars, Author.getValidationErrors() reports a "name" error;
 * for any non-blank string ≤ 150 chars, it returns no errors.
 *
 * Validates: Requirements 1.2
 */
@Label("Feature: author-management, Property 1: Name length validation")
class AuthorNameLengthValidationPropertyTest {

    /**
     * For any string longer than 150 characters, validation must report
     * a validation error for the "name" field.
     *
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 1: Name length validation")
    @Label("Names longer than 150 characters are rejected")
    void namesLongerThan150AreRejected(@ForAll("stringsOver150") String longName) {
        Author author = Author.builder().name(longName).build();

        assertFalse(author.isValid(), "Author with name > 150 chars should be invalid");
        List<ValidationError> errors = author.getValidationErrors();
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
    }

    /**
     * For any non-blank string of length ≤ 150 characters, validation must accept
     * (no validation errors).
     *
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 1: Name length validation")
    @Label("Non-blank names of 150 characters or fewer are accepted")
    void nonBlankNamesUpTo150AreAccepted(@ForAll("nonBlankStringsUpTo150") String validName) {
        Author author = Author.builder().name(validName).build();

        assertTrue(author.isValid(), "Author with valid name should be valid");
        assertTrue(author.getValidationErrors().isEmpty(),
                "Valid author should have no validation errors");
    }

    @Provide
    Arbitrary<String> stringsOver150() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(500)
                .alpha();
    }

    @Provide
    Arbitrary<String> nonBlankStringsUpTo150() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

package com.lbt;

import com.lbt.entities.Author;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 1: Name length validation
 *
 * For any string > 150 chars, ValidationHandler.validate(Author) rejects;
 * for any non-blank string ≤ 150 chars, it accepts.
 *
 * Validates: Requirements 1.2
 */
@Label("Feature: author-management, Property 1: Name length validation")
class AuthorNameLengthValidationPropertyTest {

    private final ValidationHandler validationHandler = new ValidationHandler();

    /**
     * For any string longer than 150 characters, validation must reject
     * with an IllegalArgumentException.
     *
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 1: Name length validation")
    @Label("Names longer than 150 characters are rejected")
    void namesLongerThan150AreRejected(@ForAll("stringsOver150") String longName) {
        Author author = Author.builder().name(longName).build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationHandler.validate(author),
                "Validation should reject names longer than 150 characters"
        );
        assertNotNull(ex.getMessage(), "Exception should have a descriptive message");
    }

    /**
     * For any non-blank string of length ≤ 150 characters, validation must accept
     * (no exception thrown).
     *
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 1: Name length validation")
    @Label("Non-blank names of 150 characters or fewer are accepted")
    void nonBlankNamesUpTo150AreAccepted(@ForAll("nonBlankStringsUpTo150") String validName) {
        Author author = Author.builder().name(validName).build();

        assertDoesNotThrow(
                () -> validationHandler.validate(author),
                "Validation should accept non-blank names of 150 characters or fewer"
        );
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

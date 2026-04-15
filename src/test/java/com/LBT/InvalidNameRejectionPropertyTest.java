package com.lbt;

import com.lbt.entities.Author;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 2: Invalid name rejection
 *
 * For any null or whitespace-only string, ValidationHandler.validate(Author)
 * rejects with a descriptive error message.
 *
 * Validates: Requirements 3.2, 5.3
 */
@Label("Feature: author-management, Property 2: Invalid name rejection")
class InvalidNameRejectionPropertyTest {

    private final ValidationHandler validationHandler = new ValidationHandler();

    /**
     * A null name causes rejection with IllegalArgumentException.
     *
     * Validates: Requirements 3.2, 5.3
     */
    @Example
    @Tag("Feature: author-management, Property 2: Invalid name rejection")
    @Label("Null name is rejected with IllegalArgumentException")
    void nullNameIsRejected() {
        Author author = Author.builder().name(null).build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationHandler.validate(author),
                "Validation should reject null name"
        );
        assertNotNull(ex.getMessage(), "Exception should have a descriptive message");
        assertFalse(ex.getMessage().isBlank(), "Exception message should not be blank");
    }

    /**
     * For any whitespace-only string (spaces, tabs, newlines, etc.),
     * validation rejects with IllegalArgumentException.
     *
     * Validates: Requirements 3.2, 5.3
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 2: Invalid name rejection")
    @Label("Whitespace-only names are rejected with IllegalArgumentException")
    void whitespaceOnlyNamesAreRejected(@ForAll("whitespaceOnlyStrings") String whitespaceName) {
        Author author = Author.builder().name(whitespaceName).build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationHandler.validate(author),
                "Validation should reject whitespace-only name: [" + whitespaceName.replace("\n", "\\n").replace("\t", "\\t") + "]"
        );
        assertNotNull(ex.getMessage(), "Exception should have a descriptive message");
        assertFalse(ex.getMessage().isBlank(), "Exception message should not be blank");
    }

    @Provide
    Arbitrary<String> whitespaceOnlyStrings() {
        return Arbitraries.of(' ', '\t', '\n', '\r', '\f', '\u000B')
                .list()
                .ofMinSize(1)
                .ofMaxSize(50)
                .map(chars -> {
                    StringBuilder sb = new StringBuilder();
                    for (Character c : chars) {
                        sb.append(c);
                    }
                    return sb.toString();
                });
    }
}

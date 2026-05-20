package com.lbt;

import com.lbt.entities.Author;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvalidNameRejectionPropertyTest {

    @Example
    @Tag("feature-author-management-property-2-invalid-name-rejection")
    void nullNameIsRejected() {
        Author author = Author.builder().name(null).build();

        assertFalse(author.isValid(), "Author with null name should be invalid");
        List<ValidationError> errors = author.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should have at least one validation error");
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
        assertTrue(errors.stream()
                .filter(e -> "name".equals(e.field()))
                .anyMatch(e -> e.message() != null && !e.message().isBlank()),
                "Validation error should have a descriptive message");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-2-invalid-name-rejection")
    void whitespaceOnlyNamesAreRejected(@ForAll("whitespaceOnlyStrings") String whitespaceName) {
        Author author = Author.builder().name(whitespaceName).build();

        assertFalse(author.isValid(), "Author with whitespace-only name should be invalid");
        List<ValidationError> errors = author.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should have at least one validation error");
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
        assertTrue(errors.stream()
                .filter(e -> "name".equals(e.field()))
                .anyMatch(e -> e.message() != null && !e.message().isBlank()),
                "Validation error should have a descriptive message");
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

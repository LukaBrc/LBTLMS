package com.lbt;

import com.lbt.entities.Author;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class InvalidNameRejectionPropertyTest {

    @Example
    @Tag("feature-author-management-property-2-invalid-name-rejection")
    void nullNameIsRejected() {
        Author author = Author.builder().name(null).build();

        List<ValidationError> errors = validationErrors(author);
        assertFalse(errors.isEmpty(), "Author with null name should be invalid");
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

        List<ValidationError> errors = validationErrors(author);
        assertFalse(errors.isEmpty(), "Author with whitespace-only name should be invalid");
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
        assertTrue(errors.stream()
                .filter(e -> "name".equals(e.field()))
                .anyMatch(e -> e.message() != null && !e.message().isBlank()),
                "Validation error should have a descriptive message");
    }

    @Example
    void whitespaceProviderIsAccessible() {
        assertNotNull(whitespaceOnlyStrings());
    }

    @Provide
    @SuppressWarnings("unused")
    public Arbitrary<String> whitespaceOnlyStrings() {
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

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

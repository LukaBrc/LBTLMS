package com.lbt;

import com.lbt.entities.Author;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class AuthorNameLengthValidationPropertyTest {

    @Property(tries = 100)
    @Tag("feature-author-management-property-1-name-length-validation")
    void namesLongerThan150AreRejected(@ForAll("stringsOver150") String longName) {
        Author author = Author.builder().name(longName).build();

        List<ValidationError> errors = validationErrors(author);
        assertFalse(errors.isEmpty(), "Author with name > 150 chars should be invalid");
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-1-name-length-validation")
    void nonBlankNamesUpTo150AreAccepted(@ForAll("nonBlankStringsUpTo150") String validName) {
        Author author = Author.builder().name(validName).build();

        List<ValidationError> errors = validationErrors(author);
        assertTrue(errors.isEmpty(), "Author with valid name should be valid");
    }

    @Example
    void providerMethodsAreAccessible() {
        assertNotNull(stringsOver150());
        assertNotNull(nonBlankStringsUpTo150());
    }

    @Provide
    @SuppressWarnings("unused")
    public Arbitrary<String> stringsOver150() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(500)
                .alpha();
    }

    @Provide
    @SuppressWarnings("unused")
    public Arbitrary<String> nonBlankStringsUpTo150() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

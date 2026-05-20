package com.lbt;

import com.lbt.entities.Author;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorNameLengthValidationPropertyTest {

    @Property(tries = 100)
    @Tag("feature-author-management-property-1-name-length-validation")
    void namesLongerThan150AreRejected(@ForAll("stringsOver150") String longName) {
        Author author = Author.builder().name(longName).build();

        assertFalse(author.isValid(), "Author with name > 150 chars should be invalid");
        List<ValidationError> errors = author.getValidationErrors();
        assertTrue(errors.stream().anyMatch(e -> "name".equals(e.field())),
                "Should have a validation error for the 'name' field");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-1-name-length-validation")
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

package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Feature: entity-validation-abstraction, Property 5: Error count equals the number of violated rules
/**
 * Property 5: Error count equals the number of violated rules
 *
 * For any entity implementing Validatable with N independently violated validation rules,
 * getValidationErrors() shall return a list of exactly N ValidationError entries — one per violated rule.
 *
 * Validates: Requirements 1.5, 2.5, 4.8, 5.7
 */
@Label("Feature: entity-validation-abstraction, Property 5: Error count equals the number of violated rules")
class ErrorCountPropertyTest {

    // --- Book: max 3 violatable fields (title, author, isbn) ---

    /**
     * Generate a Book with a controlled number of invalid fields (1 to 3)
     * and verify getValidationErrors().size() equals the expected count.
     *
     * Validates: Requirements 1.5, 2.5
     */
    @Property(tries = 100)
    @Label("Book error count equals number of violated fields")
    void bookErrorCountEqualsViolatedFieldCount(@ForAll("bookViolationCount") int violationCount) {
        // Fields that can be violated: title, author, isbn
        List<String> fields = new ArrayList<>(List.of("title", "author", "isbn"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        // Start with all valid values
        String title = "Valid Title";
        Author author = Author.builder().name("Valid Author").build();
        String isbn = "978-0-13-468599-1";

        // Invalidate selected fields
        for (String field : fieldsToViolate) {
            switch (field) {
                case "title" -> title = null;
                case "author" -> author = null;
                case "isbn" -> isbn = null;
            }
        }

        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .build();

        List<ValidationError> errors = book.getValidationErrors();

        assertEquals(violationCount, errors.size(),
                "Expected " + violationCount + " validation errors for Book with fields violated: "
                        + fieldsToViolate + ", but got: " + errors);
    }

    // --- Author: max 1 violatable field (name) ---

    /**
     * Generate an Author with its single field (name) violated
     * and verify getValidationErrors().size() equals 1.
     *
     * Validates: Requirements 1.5
     */
    @Property(tries = 100)
    @Label("Author error count equals 1 when name is violated")
    void authorErrorCountEqualsOne(@ForAll("blankOrNullStrings") String invalidName) {
        Author author = Author.builder()
                .name(invalidName)
                .build();

        List<ValidationError> errors = author.getValidationErrors();

        assertEquals(1, errors.size(),
                "Expected exactly 1 validation error for Author with invalid name '"
                        + invalidName + "', but got: " + errors);
    }

    // --- Member: max 3 violatable fields (name, memberId, contact) ---

    /**
     * Generate a Member with a controlled number of invalid fields (1 to 3)
     * and verify getValidationErrors().size() equals the expected count.
     *
     * Validates: Requirements 1.5, 4.8
     */
    @Property(tries = 100)
    @Label("Member error count equals number of violated fields")
    void memberErrorCountEqualsViolatedFieldCount(@ForAll("memberViolationCount") int violationCount) {
        // Fields that can be violated: name, memberId, contact
        List<String> fields = new ArrayList<>(List.of("name", "memberId", "contact"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        // Start with all valid values
        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-001");
        member.setContact("valid@example.com");

        // Invalidate selected fields
        for (String field : fieldsToViolate) {
            switch (field) {
                case "name" -> member.setName(null);
                case "memberId" -> member.setMemberId(null);
                case "contact" -> member.setContact(null);
            }
        }

        List<ValidationError> errors = member.getValidationErrors();

        assertEquals(violationCount, errors.size(),
                "Expected " + violationCount + " validation errors for Member with fields violated: "
                        + fieldsToViolate + ", but got: " + errors);
    }

    // --- BorrowTransaction: max 3 violatable fields (bookIsbn, memberId, borrowDate) ---

    /**
     * Generate a BorrowTransaction with a controlled number of invalid fields (1 to 3)
     * and verify getValidationErrors().size() equals the expected count.
     *
     * Validates: Requirements 1.5, 5.7
     */
    @Property(tries = 100)
    @Label("BorrowTransaction error count equals number of violated fields")
    void borrowTransactionErrorCountEqualsViolatedFieldCount(
            @ForAll("borrowTransactionViolationCount") int violationCount) {
        // Fields that can be violated: bookIsbn, memberId, borrowDate
        List<String> fields = new ArrayList<>(List.of("bookIsbn", "memberId", "borrowDate"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        // Start with all valid values
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-13-468599-1");
        tx.setMemberId("VALID-001");
        tx.setBorrowDate(LocalDate.now());

        // Invalidate selected fields
        for (String field : fieldsToViolate) {
            switch (field) {
                case "bookIsbn" -> tx.setBookIsbn(null);
                case "memberId" -> tx.setMemberId(null);
                case "borrowDate" -> tx.setBorrowDate(null);
            }
        }

        List<ValidationError> errors = tx.getValidationErrors();

        assertEquals(violationCount, errors.size(),
                "Expected " + violationCount + " validation errors for BorrowTransaction with fields violated: "
                        + fieldsToViolate + ", but got: " + errors);
    }

    // --- Generators ---

    @Provide
    Arbitrary<Integer> bookViolationCount() {
        return Arbitraries.integers().between(1, 3);
    }

    @Provide
    Arbitrary<Integer> memberViolationCount() {
        return Arbitraries.integers().between(1, 3);
    }

    @Provide
    Arbitrary<Integer> borrowTransactionViolationCount() {
        return Arbitraries.integers().between(1, 3);
    }

    @Provide
    Arbitrary<String> blankOrNullStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.strings()
                        .withChars(' ', '\t', '\n', '\r')
                        .ofMinLength(1)
                        .ofMaxLength(20)
        );
    }
}

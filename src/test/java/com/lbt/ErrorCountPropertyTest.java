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

class ErrorCountPropertyTest {


    @Property(tries = 100)
    void bookErrorCountEqualsViolatedFieldCount(@ForAll("bookViolationCount") int violationCount) {
        List<String> fields = new ArrayList<>(List.of("title", "author", "isbn"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        String title = "Valid Title";
        Author author = Author.builder().name("Valid Author").build();
        String isbn = "978-0-13-468599-1";

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


    @Property(tries = 100)
    void authorErrorCountEqualsOne(@ForAll("blankOrNullStrings") String invalidName) {
        Author author = Author.builder()
                .name(invalidName)
                .build();

        List<ValidationError> errors = author.getValidationErrors();

        assertEquals(1, errors.size(),
                "Expected exactly 1 validation error for Author with invalid name '"
                        + invalidName + "', but got: " + errors);
    }


    @Property(tries = 100)
    void memberErrorCountEqualsViolatedFieldCount(@ForAll("memberViolationCount") int violationCount) {
        List<String> fields = new ArrayList<>(List.of("name", "memberId", "contact"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-001");
        member.setContact("valid@example.com");

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


    @Property(tries = 100)
    void borrowTransactionErrorCountEqualsViolatedFieldCount(
            @ForAll("borrowTransactionViolationCount") int violationCount) {
        List<String> fields = new ArrayList<>(List.of("bookIsbn", "memberId", "borrowDate"));
        Collections.shuffle(fields);
        List<String> fieldsToViolate = fields.subList(0, violationCount);

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-13-468599-1");
        tx.setMemberId("VALID-001");
        tx.setBorrowDate(LocalDate.now());

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

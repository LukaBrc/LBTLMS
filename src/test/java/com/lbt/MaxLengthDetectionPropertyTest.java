package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Feature: entity-validation-abstraction, Property 3: Fields exceeding maximum length produce validation errors
/**
 * Property 3: Fields exceeding maximum length produce validation errors
 *
 * For any entity with a length-constrained field set to a non-blank string
 * exceeding the maximum length, getValidationErrors() shall contain a
 * ValidationError whose field property matches that field's name.
 *
 * Validates: Requirements 3.3, 4.5, 4.6, 4.7, 5.5, 5.6
 */
@Label("Feature: entity-validation-abstraction, Property 3: Fields exceeding maximum length produce validation errors")
class MaxLengthDetectionPropertyTest {

    // --- Author: name (max 150) ---

    /**
     * Validates: Requirements 3.3
     */
    @Property(tries = 100)
    @Label("Author name exceeding 150 characters produces validation error")
    void authorNameExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver150") String longName) {

        Author author = Author.builder().name(longName).build();

        List<ValidationError> errors = author.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name length is " + longName.length()
        );
    }

    // --- Member: name (max 150) ---

    /**
     * Validates: Requirements 4.5
     */
    @Property(tries = 100)
    @Label("Member name exceeding 150 characters produces validation error")
    void memberNameExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver150") String longName) {

        Member member = new Member();
        member.setName(longName);
        member.setMemberId("VALID-ID");
        member.setContact("valid@contact.com");

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name length is " + longName.length()
        );
    }

    // --- Member: memberId (max 50) ---

    /**
     * Validates: Requirements 4.6
     */
    @Property(tries = 100)
    @Label("Member memberId exceeding 50 characters produces validation error")
    void memberMemberIdExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longMemberId) {

        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId(longMemberId);
        member.setContact("valid@contact.com");

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId length is " + longMemberId.length()
        );
    }

    // --- Member: contact (max 200) ---

    /**
     * Validates: Requirements 4.7
     */
    @Property(tries = 100)
    @Label("Member contact exceeding 200 characters produces validation error")
    void memberContactExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver200") String longContact) {

        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-ID");
        member.setContact(longContact);

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("contact")),
                "Expected a ValidationError for field 'contact' when contact length is " + longContact.length()
        );
    }

    // --- BorrowTransaction: bookIsbn (max 50) ---

    /**
     * Validates: Requirements 5.5
     */
    @Property(tries = 100)
    @Label("BorrowTransaction bookIsbn exceeding 50 characters produces validation error")
    void borrowTransactionBookIsbnExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longIsbn) {

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(longIsbn);
        tx.setMemberId("VALID-ID");
        tx.setBorrowDate(LocalDate.now());

        List<ValidationError> errors = tx.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("bookIsbn")),
                "Expected a ValidationError for field 'bookIsbn' when bookIsbn length is " + longIsbn.length()
        );
    }

    // --- BorrowTransaction: memberId (max 50) ---

    /**
     * Validates: Requirements 5.6
     */
    @Property(tries = 100)
    @Label("BorrowTransaction memberId exceeding 50 characters produces validation error")
    void borrowTransactionMemberIdExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longMemberId) {

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-123456-47-2");
        tx.setMemberId(longMemberId);
        tx.setBorrowDate(LocalDate.now());

        List<ValidationError> errors = tx.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId length is " + longMemberId.length()
        );
    }

    // --- Generators ---

    @Provide
    Arbitrary<String> nonBlankStringsOver150() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(500)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> nonBlankStringsOver50() {
        return Arbitraries.strings()
                .ofMinLength(51)
                .ofMaxLength(200)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> nonBlankStringsOver200() {
        return Arbitraries.strings()
                .ofMinLength(201)
                .ofMaxLength(600)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

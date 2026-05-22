package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class MaxLengthDetectionPropertyTest {


    @Property(tries = 100)
    void authorNameExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver150") String longName) {

        Author author = Author.builder().name(longName).build();

        List<ValidationError> errors = validationErrors(author);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name length is " + longName.length()
        );
    }


    @Property(tries = 100)
    void memberNameExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver150") String longName) {

        Member member = new Member();
        member.setName(longName);
        member.setMemberId("VALID-ID");
        member.setContact("valid@contact.com");

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name length is " + longName.length()
        );
    }


    @Property(tries = 100)
    void memberMemberIdExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longMemberId) {

        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId(longMemberId);
        member.setContact("valid@contact.com");

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId length is " + longMemberId.length()
        );
    }


    @Property(tries = 100)
    void memberContactExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver200") String longContact) {

        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-ID");
        member.setContact(longContact);

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("contact")),
                "Expected a ValidationError for field 'contact' when contact length is " + longContact.length()
        );
    }


    @Property(tries = 100)
    void borrowTransactionBookIsbnExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longIsbn) {

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(longIsbn);
        tx.setMemberId("VALID-ID");
        tx.setBorrowDate(LocalDate.now());

        List<ValidationError> errors = validationErrors(tx);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("bookIsbn")),
                "Expected a ValidationError for field 'bookIsbn' when bookIsbn length is " + longIsbn.length()
        );
    }


    @Property(tries = 100)
    void borrowTransactionMemberIdExceedingMaxLengthProducesError(
            @ForAll("nonBlankStringsOver50") String longMemberId) {

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-123456-47-2");
        tx.setMemberId(longMemberId);
        tx.setBorrowDate(LocalDate.now());

        List<ValidationError> errors = validationErrors(tx);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId length is " + longMemberId.length()
        );
    }


    @Provide
    public Arbitrary<String> nonBlankStringsOver150() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(500)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    public Arbitrary<String> nonBlankStringsOver50() {
        return Arbitraries.strings()
                .ofMinLength(51)
                .ofMaxLength(200)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    public Arbitrary<String> nonBlankStringsOver200() {
        return Arbitraries.strings()
                .ofMinLength(201)
                .ofMaxLength(600)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

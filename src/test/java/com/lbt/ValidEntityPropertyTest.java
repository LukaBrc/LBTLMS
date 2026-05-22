package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class ValidEntityPropertyTest {

    @Property(tries = 100)
    @Tag("feature-entity-validation-abstraction-property-4-valid-entities-no-validation-errors")
    void validAuthorProducesNoErrors(@ForAll("validAuthorNames") String name) {
        Author author = Author.builder().name(name).build();

        assertTrue(ValidationHandlerResolver.get().isValid(author), "Valid Author should return isValid() == true");
        assertTrue(ValidationHandlerResolver.get().getValidationErrors(author).isEmpty(),
                "Valid Author should have no validation errors");
    }

    @Property(tries = 100)
    @Tag("feature-entity-validation-abstraction-property-4-valid-entities-no-validation-errors")
    void validBookProducesNoErrors(
            @ForAll("validBookTitles") String title,
            @ForAll("validAuthorNames") String authorName,
            @ForAll("validIsbns") String isbn) {

        Author author = Author.builder().name(authorName).build();
        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .build();

        assertTrue(ValidationHandlerResolver.get().isValid(book), "Valid Book should return isValid() == true");
        assertTrue(ValidationHandlerResolver.get().getValidationErrors(book).isEmpty(),
                "Valid Book should have no validation errors");
    }

    @Property(tries = 100)
    @Tag("feature-entity-validation-abstraction-property-4-valid-entities-no-validation-errors")
    void validMemberProducesNoErrors(
            @ForAll("validMemberNames") String name,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validContacts") String contact) {

        Member member = new Member();
        member.setName(name);
        member.setMemberId(memberId);
        member.setContact(contact);

        assertTrue(ValidationHandlerResolver.get().isValid(member), "Valid Member should return isValid() == true");
        assertTrue(ValidationHandlerResolver.get().getValidationErrors(member).isEmpty(),
                "Valid Member should have no validation errors");
    }

    @Property(tries = 100)
    @Tag("feature-entity-validation-abstraction-property-4-valid-entities-no-validation-errors")
    void validBorrowTransactionProducesNoErrors(
            @ForAll("validIsbns") String bookIsbn,
            @ForAll("validMemberIds") String memberId,
            @ForAll("validDates") LocalDate borrowDate) {

        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setBookIsbn(bookIsbn);
        transaction.setMemberId(memberId);
        transaction.setBorrowDate(borrowDate);

        assertTrue(ValidationHandlerResolver.get().isValid(transaction), "Valid BorrowTransaction should return isValid() == true");
        assertTrue(ValidationHandlerResolver.get().getValidationErrors(transaction).isEmpty(),
                "Valid BorrowTransaction should have no validation errors");
    }


    @Provide
    Arbitrary<String> validAuthorNames() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> validBookTitles() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> validIsbns() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(50)
                .withCharRange('0', '9')
                .withCharRange('A', 'Z')
                .withChars('-')
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> validMemberNames() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> validMemberIds() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(50)
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> validContacts() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<LocalDate> validDates() {
        return Arbitraries.integers().between(2000, 2030).flatMap(year ->
                Arbitraries.integers().between(1, 12).flatMap(month ->
                        Arbitraries.integers().between(1, 28).map(day ->
                                LocalDate.of(year, month, day)
                        )
                )
        );
    }
}

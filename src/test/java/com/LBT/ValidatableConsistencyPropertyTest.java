package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.Validatable;

import net.jqwik.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Feature: entity-validation-abstraction, Property 1: isValid and getValidationErrors consistency
/**
 * Property 1: isValid and getValidationErrors consistency
 *
 * For any entity implementing Validatable, isValid() returns true if and only if
 * getValidationErrors() returns an empty list.
 *
 * Validates: Requirements 1.3, 1.4
 */
@Label("Feature: entity-validation-abstraction, Property 1: isValid and getValidationErrors consistency")
class ValidatableConsistencyPropertyTest {

    /**
     * For any randomly generated Book entity (with mix of valid and invalid fields),
     * isValid() must equal getValidationErrors().isEmpty().
     *
     * Validates: Requirements 1.3, 1.4
     */
    @Property(tries = 100)
    @Label("Book: isValid() == getValidationErrors().isEmpty()")
    void bookIsValidConsistentWithGetValidationErrors(
            @ForAll("randomBooks") Book book) {
        assertConsistency(book);
    }

    /**
     * For any randomly generated Author entity (with mix of valid and invalid fields),
     * isValid() must equal getValidationErrors().isEmpty().
     *
     * Validates: Requirements 1.3, 1.4
     */
    @Property(tries = 100)
    @Label("Author: isValid() == getValidationErrors().isEmpty()")
    void authorIsValidConsistentWithGetValidationErrors(
            @ForAll("randomAuthors") Author author) {
        assertConsistency(author);
    }

    /**
     * For any randomly generated Member entity (with mix of valid and invalid fields),
     * isValid() must equal getValidationErrors().isEmpty().
     *
     * Validates: Requirements 1.3, 1.4
     */
    @Property(tries = 100)
    @Label("Member: isValid() == getValidationErrors().isEmpty()")
    void memberIsValidConsistentWithGetValidationErrors(
            @ForAll("randomMembers") Member member) {
        assertConsistency(member);
    }

    /**
     * For any randomly generated BorrowTransaction entity (with mix of valid and invalid fields),
     * isValid() must equal getValidationErrors().isEmpty().
     *
     * Validates: Requirements 1.3, 1.4
     */
    @Property(tries = 100)
    @Label("BorrowTransaction: isValid() == getValidationErrors().isEmpty()")
    void borrowTransactionIsValidConsistentWithGetValidationErrors(
            @ForAll("randomBorrowTransactions") BorrowTransaction transaction) {
        assertConsistency(transaction);
    }

    private void assertConsistency(Validatable entity) {
        boolean isValid = entity.isValid();
        boolean errorsEmpty = entity.getValidationErrors().isEmpty();
        assertEquals(isValid, errorsEmpty,
                String.format("isValid()=%s but getValidationErrors().isEmpty()=%s for %s",
                        isValid, errorsEmpty, entity));
    }

    @Provide
    Arbitrary<Book> randomBooks() {
        Arbitrary<String> titles = nullableString();
        Arbitrary<Author> authors = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.of(Author.builder().name("Valid Author").build())
        );
        Arbitrary<String> isbns = nullableString();

        return Combinators.combine(titles, authors, isbns)
                .as((title, author, isbn) -> {
                    Book book = new Book();
                    book.setTitle(title);
                    book.setAuthor(author);
                    book.setIsbn(isbn);
                    return book;
                });
    }

    @Provide
    Arbitrary<Author> randomAuthors() {
        return nullableStringWithLength().map(name -> Author.builder().name(name).build());
    }

    @Provide
    Arbitrary<Member> randomMembers() {
        Arbitrary<String> names = nullableStringWithLength();
        Arbitrary<String> memberIds = nullableStringWithId();
        Arbitrary<String> contacts = nullableStringWithContact();

        return Combinators.combine(names, memberIds, contacts)
                .as((name, memberId, contact) -> {
                    Member member = new Member();
                    member.setName(name);
                    member.setMemberId(memberId);
                    member.setContact(contact);
                    return member;
                });
    }

    @Provide
    Arbitrary<BorrowTransaction> randomBorrowTransactions() {
        Arbitrary<String> bookIsbns = nullableStringWithId();
        Arbitrary<String> memberIds = nullableStringWithId();
        Arbitrary<LocalDate> borrowDates = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.of(LocalDate.of(2024, 1, 15), LocalDate.of(2023, 6, 1), LocalDate.now())
        );

        return Combinators.combine(bookIsbns, memberIds, borrowDates)
                .as((bookIsbn, memberId, borrowDate) -> {
                    BorrowTransaction tx = new BorrowTransaction();
                    tx.setBookIsbn(bookIsbn);
                    tx.setMemberId(memberId);
                    tx.setBorrowDate(borrowDate);
                    return tx;
                });
    }

    /**
     * Generates nullable strings: null, empty, whitespace-only, or valid non-blank strings.
     */
    private Arbitrary<String> nullableString() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.just("\t\n"),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(50).alpha()
                        .filter(s -> !s.trim().isEmpty())
        );
    }

    /**
     * Generates nullable strings including over-length values for name fields (max 150).
     */
    private Arbitrary<String> nullableStringWithLength() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(150).alpha()
                        .filter(s -> !s.trim().isEmpty()),
                Arbitraries.strings().ofMinLength(151).ofMaxLength(200).alpha()
        );
    }

    /**
     * Generates nullable strings including over-length values for ID fields (max 50).
     */
    private Arbitrary<String> nullableStringWithId() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(50).alpha()
                        .filter(s -> !s.trim().isEmpty()),
                Arbitraries.strings().ofMinLength(51).ofMaxLength(100).alpha()
        );
    }

    /**
     * Generates nullable strings including over-length values for contact fields (max 200).
     */
    private Arbitrary<String> nullableStringWithContact() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(200).alpha()
                        .filter(s -> !s.trim().isEmpty()),
                Arbitraries.strings().ofMinLength(201).ofMaxLength(300).alpha()
        );
    }
}

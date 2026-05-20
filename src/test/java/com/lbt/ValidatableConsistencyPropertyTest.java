package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.Validatable;

import net.jqwik.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatableConsistencyPropertyTest {

    @Property(tries = 100)
    void bookIsValidConsistentWithGetValidationErrors(
            @ForAll("randomBooks") Book book) {
        assertConsistency(book);
    }

    @Property(tries = 100)
    void authorIsValidConsistentWithGetValidationErrors(
            @ForAll("randomAuthors") Author author) {
        assertConsistency(author);
    }

    @Property(tries = 100)
    void memberIsValidConsistentWithGetValidationErrors(
            @ForAll("randomMembers") Member member) {
        assertConsistency(member);
    }

    @Property(tries = 100)
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

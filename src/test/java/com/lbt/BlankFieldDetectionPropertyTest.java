package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Feature: entity-validation-abstraction, Property 2: Blank required fields produce validation errors
/**
 * Property 2: Blank required fields produce validation errors
 *
 * For any entity implementing Validatable and for any required string field on that entity,
 * if the field value is null or composed entirely of whitespace characters, then
 * getValidationErrors() shall contain a ValidationError whose field property matches that field's name.
 *
 * Validates: Requirements 2.2, 2.4, 3.2, 4.2, 4.3, 4.4, 5.2, 5.3
 */
@Label("Feature: entity-validation-abstraction, Property 2: Blank required fields produce validation errors")
class BlankFieldDetectionPropertyTest {

    // --- Book: title (null/blank) ---

    /**
     * Validates: Requirements 2.2
     */
    @Property(tries = 100)
    @Label("Book with blank/null title produces validation error for 'title'")
    void bookBlankTitleProducesError(@ForAll("blankOrNullStrings") String blankTitle) {
        Book book = Book.builder()
                .title(blankTitle)
                .isbn("978-0-13-468599-1")
                .author(Author.builder().name("Valid Author").build())
                .build();

        List<ValidationError> errors = book.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("title")),
                "Expected a ValidationError for field 'title' when title is blank/null, got: " + errors
        );
    }

    // --- Book: isbn (null/blank) ---

    /**
     * Validates: Requirements 2.4
     */
    @Property(tries = 100)
    @Label("Book with blank/null isbn produces validation error for 'isbn'")
    void bookBlankIsbnProducesError(@ForAll("blankOrNullStrings") String blankIsbn) {
        Book book = Book.builder()
                .title("Valid Title")
                .isbn(blankIsbn)
                .author(Author.builder().name("Valid Author").build())
                .build();

        List<ValidationError> errors = book.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("isbn")),
                "Expected a ValidationError for field 'isbn' when isbn is blank/null, got: " + errors
        );
    }

    // --- Book: author (null) ---

    /**
     * Validates: Requirements 2.2
     */
    @Property(tries = 100)
    @Label("Book with null author produces validation error for 'author'")
    void bookNullAuthorProducesError(@ForAll("validNonBlankStrings") String title,
                                     @ForAll("validNonBlankStrings") String isbn) {
        Book book = Book.builder()
                .title(title)
                .isbn(isbn)
                .author(null)
                .build();

        List<ValidationError> errors = book.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("author")),
                "Expected a ValidationError for field 'author' when author is null, got: " + errors
        );
    }

    // --- Author: name (null/blank) ---

    /**
     * Validates: Requirements 3.2
     */
    @Property(tries = 100)
    @Label("Author with blank/null name produces validation error for 'name'")
    void authorBlankNameProducesError(@ForAll("blankOrNullStrings") String blankName) {
        Author author = Author.builder()
                .name(blankName)
                .build();

        List<ValidationError> errors = author.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name is blank/null, got: " + errors
        );
    }

    // --- Member: name (null/blank) ---

    /**
     * Validates: Requirements 4.2
     */
    @Property(tries = 100)
    @Label("Member with blank/null name produces validation error for 'name'")
    void memberBlankNameProducesError(@ForAll("blankOrNullStrings") String blankName) {
        Member member = new Member();
        member.setName(blankName);
        member.setMemberId("VALID-001");
        member.setContact("valid@example.com");

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name is blank/null, got: " + errors
        );
    }

    // --- Member: memberId (null/blank) ---

    /**
     * Validates: Requirements 4.3
     */
    @Property(tries = 100)
    @Label("Member with blank/null memberId produces validation error for 'memberId'")
    void memberBlankMemberIdProducesError(@ForAll("blankOrNullStrings") String blankMemberId) {
        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId(blankMemberId);
        member.setContact("valid@example.com");

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId is blank/null, got: " + errors
        );
    }

    // --- Member: contact (null/blank) ---

    /**
     * Validates: Requirements 4.4
     */
    @Property(tries = 100)
    @Label("Member with blank/null contact produces validation error for 'contact'")
    void memberBlankContactProducesError(@ForAll("blankOrNullStrings") String blankContact) {
        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-001");
        member.setContact(blankContact);

        List<ValidationError> errors = member.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("contact")),
                "Expected a ValidationError for field 'contact' when contact is blank/null, got: " + errors
        );
    }

    // --- BorrowTransaction: bookIsbn (null/blank) ---

    /**
     * Validates: Requirements 5.2
     */
    @Property(tries = 100)
    @Label("BorrowTransaction with blank/null bookIsbn produces validation error for 'bookIsbn'")
    void borrowTransactionBlankBookIsbnProducesError(@ForAll("blankOrNullStrings") String blankBookIsbn) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(blankBookIsbn);
        tx.setMemberId("VALID-001");
        tx.setBorrowDate(java.time.LocalDate.now());

        List<ValidationError> errors = tx.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("bookIsbn")),
                "Expected a ValidationError for field 'bookIsbn' when bookIsbn is blank/null, got: " + errors
        );
    }

    // --- BorrowTransaction: memberId (null/blank) ---

    /**
     * Validates: Requirements 5.3
     */
    @Property(tries = 100)
    @Label("BorrowTransaction with blank/null memberId produces validation error for 'memberId'")
    void borrowTransactionBlankMemberIdProducesError(@ForAll("blankOrNullStrings") String blankMemberId) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-13-468599-1");
        tx.setMemberId(blankMemberId);
        tx.setBorrowDate(java.time.LocalDate.now());

        List<ValidationError> errors = tx.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId is blank/null, got: " + errors
        );
    }

    // --- BorrowTransaction: borrowDate (null) ---

    /**
     * Validates: Requirements 5.2
     */
    @Property(tries = 100)
    @Label("BorrowTransaction with null borrowDate produces validation error for 'borrowDate'")
    void borrowTransactionNullBorrowDateProducesError(@ForAll("validNonBlankStrings") String bookIsbn,
                                                      @ForAll("validNonBlankStrings") String memberId) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(bookIsbn);
        tx.setMemberId(memberId);
        tx.setBorrowDate(null);

        List<ValidationError> errors = tx.getValidationErrors();

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("borrowDate")),
                "Expected a ValidationError for field 'borrowDate' when borrowDate is null, got: " + errors
        );
    }

    // --- Generators ---

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

    @Provide
    Arbitrary<String> validNonBlankStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(30)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

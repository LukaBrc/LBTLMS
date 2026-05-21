package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class BlankFieldDetectionPropertyTest {


    @Property(tries = 100)
    void bookBlankTitleProducesError(@ForAll("blankOrNullStrings") String blankTitle) {
        Book book = Book.builder()
                .title(blankTitle)
                .isbn("978-0-13-468599-1")
                .author(Author.builder().name("Valid Author").build())
                .build();

        List<ValidationError> errors = validationErrors(book);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("title")),
                "Expected a ValidationError for field 'title' when title is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void bookBlankIsbnProducesError(@ForAll("blankOrNullStrings") String blankIsbn) {
        Book book = Book.builder()
                .title("Valid Title")
                .isbn(blankIsbn)
                .author(Author.builder().name("Valid Author").build())
                .build();

        List<ValidationError> errors = validationErrors(book);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("isbn")),
                "Expected a ValidationError for field 'isbn' when isbn is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void bookNullAuthorProducesError(@ForAll("validNonBlankStrings") String title,
                                     @ForAll("validNonBlankStrings") String isbn) {
        Book book = Book.builder()
                .title(title)
                .isbn(isbn)
                .author(null)
                .build();

        List<ValidationError> errors = validationErrors(book);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("author")),
                "Expected a ValidationError for field 'author' when author is null, got: " + errors
        );
    }


    @Property(tries = 100)
    void authorBlankNameProducesError(@ForAll("blankOrNullStrings") String blankName) {
        Author author = Author.builder()
                .name(blankName)
                .build();

        List<ValidationError> errors = validationErrors(author);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void memberBlankNameProducesError(@ForAll("blankOrNullStrings") String blankName) {
        Member member = new Member();
        member.setName(blankName);
        member.setMemberId("VALID-001");
        member.setContact("valid@example.com");

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("name")),
                "Expected a ValidationError for field 'name' when name is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void memberBlankMemberIdProducesError(@ForAll("blankOrNullStrings") String blankMemberId) {
        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId(blankMemberId);
        member.setContact("valid@example.com");

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void memberBlankContactProducesError(@ForAll("blankOrNullStrings") String blankContact) {
        Member member = new Member();
        member.setName("Valid Name");
        member.setMemberId("VALID-001");
        member.setContact(blankContact);

        List<ValidationError> errors = validationErrors(member);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("contact")),
                "Expected a ValidationError for field 'contact' when contact is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void borrowTransactionBlankBookIsbnProducesError(@ForAll("blankOrNullStrings") String blankBookIsbn) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(blankBookIsbn);
        tx.setMemberId("VALID-001");
        tx.setBorrowDate(java.time.LocalDate.now());

        List<ValidationError> errors = validationErrors(tx);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("bookIsbn")),
                "Expected a ValidationError for field 'bookIsbn' when bookIsbn is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void borrowTransactionBlankMemberIdProducesError(@ForAll("blankOrNullStrings") String blankMemberId) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("978-0-13-468599-1");
        tx.setMemberId(blankMemberId);
        tx.setBorrowDate(java.time.LocalDate.now());

        List<ValidationError> errors = validationErrors(tx);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("memberId")),
                "Expected a ValidationError for field 'memberId' when memberId is blank/null, got: " + errors
        );
    }


    @Property(tries = 100)
    void borrowTransactionNullBorrowDateProducesError(@ForAll("validNonBlankStrings") String bookIsbn,
                                                      @ForAll("validNonBlankStrings") String memberId) {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(bookIsbn);
        tx.setMemberId(memberId);
        tx.setBorrowDate(null);

        List<ValidationError> errors = validationErrors(tx);

        assertTrue(
                errors.stream().anyMatch(e -> e.field().equals("borrowDate")),
                "Expected a ValidationError for field 'borrowDate' when borrowDate is null, got: " + errors
        );
    }


    @Provide
    public Arbitrary<String> blankOrNullStrings() {
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
    public Arbitrary<String> validNonBlankStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(30)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

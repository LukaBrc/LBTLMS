package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.AuthorRepository;
import com.lbt.repositories.BookRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.BookCache;
import com.lbt.services.BookService;
import com.lbt.validation.ValidationError;

import net.jqwik.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Feature: entity-validation-abstraction, Property 6: Backward-compatible error messages for Book and Author
/**
 * Property 6: Backward-compatible error messages for Book and Author
 *
 * For any invalid Book or Author entity passed to its respective service method, the service
 * shall throw an IllegalArgumentException whose message exactly matches the message that the
 * current ValidationHandler would produce for the same invalid state, using the same evaluation
 * order (null-object check first, then field checks in declaration order, stopping at the first failure).
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4
 */
@Label("Feature: entity-validation-abstraction, Property 6: Backward-compatible error messages for Book and Author")
class BackwardCompatMessagePropertyTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final AuthorService authorServiceMock = mock(AuthorService.class);
    private final BookCache bookCache;
    private final BookService bookService;

    {
        BookRepository cacheRepo = mock(BookRepository.class);
        when(cacheRepo.findAll()).thenReturn(Collections.emptyList());
        bookCache = new BookCache(cacheRepo);
        bookCache.init();
        bookService = new BookService(bookRepository, authorServiceMock, bookCache);
    }

    private final AuthorRepository authorRepository = mock(AuthorRepository.class);
    private final AuthorCache authorCache = mock(AuthorCache.class);
    private final AuthorService authorService = new AuthorService(authorRepository, authorCache);

    // --- BookService: null book ---

    /**
     * Validates: Requirements 7.1, 7.4
     */
    @Property(tries = 100)
    @Label("BookService.addBook(null) throws IllegalArgumentException with 'Book must not be null.'")
    void bookServiceNullBookThrowsExpectedMessage(@ForAll("validNonBlankStrings") String ignored) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(null)
        );

        String expectedMessage = "Book must not be null.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler null-book message");
    }

    // --- BookService: blank title (first field in declaration order) ---

    /**
     * Validates: Requirements 7.1, 7.3, 7.4
     */
    @Property(tries = 100)
    @Label("BookService.addBook(book with blank title) throws first error matching ValidationHandler")
    void bookServiceBlankTitleThrowsExpectedMessage(@ForAll("blankOrNullStrings") String blankTitle) {
        Book book = Book.builder()
                .title(blankTitle)
                .author(Author.builder().name("Valid Author").build())
                .isbn("978-0-13-468599-1")
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(book)
        );

        // ValidationHandler evaluation order: title first
        String expectedMessage = "Book title must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-title message");
    }

    // --- BookService: null author (second field in declaration order) ---

    /**
     * Validates: Requirements 7.1, 7.3, 7.4
     */
    @Property(tries = 100)
    @Label("BookService.addBook(book with null author) throws first error matching ValidationHandler")
    void bookServiceNullAuthorThrowsExpectedMessage(@ForAll("validNonBlankStrings") String title,
                                                     @ForAll("validNonBlankStrings") String isbn) {
        Book book = Book.builder()
                .title(title)
                .author(null)
                .isbn(isbn)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(book)
        );

        // ValidationHandler evaluation order: title OK, author null → first error
        String expectedMessage = "Book author must not be null.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler null-author message");
    }

    // --- BookService: blank isbn (third field in declaration order) ---

    /**
     * Validates: Requirements 7.1, 7.3, 7.4
     */
    @Property(tries = 100)
    @Label("BookService.addBook(book with blank isbn) throws first error matching ValidationHandler")
    void bookServiceBlankIsbnThrowsExpectedMessage(@ForAll("blankOrNullStrings") String blankIsbn) {
        Book book = Book.builder()
                .title("Valid Title")
                .author(Author.builder().name("Valid Author").build())
                .isbn(blankIsbn)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(book)
        );

        // ValidationHandler evaluation order: title OK, author OK, isbn blank → first error
        String expectedMessage = "Book ISBN must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-isbn message");
    }

    // --- BookService: multiple invalid fields → first error only ---

    /**
     * Validates: Requirements 7.4
     */
    @Property(tries = 100)
    @Label("BookService.addBook(book with multiple invalid fields) throws only the first error")
    void bookServiceMultipleInvalidFieldsThrowsFirstError(
            @ForAll("blankOrNullStrings") String blankTitle,
            @ForAll("blankOrNullStrings") String blankIsbn) {
        Book book = Book.builder()
                .title(blankTitle)
                .author(null)
                .isbn(blankIsbn)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(book)
        );

        // ValidationHandler evaluation order: title is first → stops at first failure
        List<ValidationError> errors = book.getValidationErrors();
        String expectedMessage = errors.get(0).message();
        assertEquals(expectedMessage, ex.getMessage(),
                "Service must throw the first validation error message (declaration order)");
    }

    // --- AuthorService: null/blank name ---

    /**
     * Validates: Requirements 7.2, 7.3, 7.4
     */
    @Property(tries = 100)
    @Label("AuthorService.createAuthor(blank name) throws 'Author name must not be empty.'")
    void authorServiceBlankNameThrowsExpectedMessage(@ForAll("blankOrNullStrings") String blankName) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(blankName)
        );

        String expectedMessage = "Author name must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-name message");
    }

    // --- AuthorService: name exceeds 150 characters ---

    /**
     * Validates: Requirements 7.2, 7.3, 7.4
     */
    @Property(tries = 100)
    @Label("AuthorService.createAuthor(name > 150 chars) throws 'Author name must not exceed 150 characters.'")
    void authorServiceLongNameThrowsExpectedMessage(@ForAll("longNames") String longName) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(longName)
        );

        String expectedMessage = "Author name must not exceed 150 characters.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler long-name message");
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

    @Provide
    Arbitrary<String> longNames() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(300)
                .alpha();
    }
}

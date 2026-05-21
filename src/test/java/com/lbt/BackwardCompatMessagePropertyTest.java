package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.AuthorRepository;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.BookCache;
import com.lbt.services.BookService;
import com.lbt.validation.ValidationError;
import com.lbt.validation.ValidationHandlerResolver;

import net.jqwik.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
class BackwardCompatMessagePropertyTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BorrowTransactionRepository borrowTransactionRepository = mock(BorrowTransactionRepository.class);
    private final AuthorService authorServiceMock = mock(AuthorService.class);
    private final BookService bookService;

    {
        BookRepository cacheRepo = mock(BookRepository.class);
        when(cacheRepo.findAllByDeletedFalse()).thenReturn(Collections.emptyList());
        BookCache bookCache = new BookCache(cacheRepo);
        bookCache.init();
        bookService = new BookService(bookRepository, authorServiceMock, bookCache, borrowTransactionRepository);
    }

    private final AuthorRepository authorRepository = mock(AuthorRepository.class);
    private final AuthorCache authorCache = mock(AuthorCache.class);
    private final AuthorService authorService = new AuthorService(authorRepository, authorCache);


    @Property(tries = 100)
    void bookServiceNullBookThrowsExpectedMessage(@ForAll("validNonBlankStrings") String ignored) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.addBook(null)
        );

        String expectedMessage = "Book must not be null.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler null-book message");
    }


    @Property(tries = 100)
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

        String expectedMessage = "Book title must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-title message");
    }


    @Property(tries = 100)
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

        String expectedMessage = "Book author must not be null.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler null-author message");
    }


    @Property(tries = 100)
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

        String expectedMessage = "Book ISBN must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-isbn message");
    }


    @Property(tries = 100)
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

        List<ValidationError> errors = validationErrors(book);
        String expectedMessage = errors.get(0).message();
        assertEquals(expectedMessage, ex.getMessage(),
                "Service must throw the first validation error message (declaration order)");
    }


    @Property(tries = 100)
    void authorServiceBlankNameThrowsExpectedMessage(@ForAll("blankOrNullStrings") String blankName) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(blankName)
        );

        String expectedMessage = "Author name must not be empty.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler blank-name message");
    }


    @Property(tries = 100)
    void authorServiceLongNameThrowsExpectedMessage(@ForAll("longNames") String longName) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.createAuthor(longName)
        );

        String expectedMessage = "Author name must not exceed 150 characters.";
        assertEquals(expectedMessage, ex.getMessage(),
                "Service message must match ValidationHandler long-name message");
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

    @Provide
    public Arbitrary<String> longNames() {
        return Arbitraries.strings()
                .ofMinLength(151)
                .ofMaxLength(300)
                .alpha();
    }

    private List<ValidationError> validationErrors(Object entity) {
        return ValidationHandlerResolver.get().getValidationErrors(entity);
    }
}

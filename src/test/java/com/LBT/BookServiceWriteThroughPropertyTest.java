package com.lbt;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.services.AuthorService;
import com.lbt.services.BookCache;
import com.lbt.services.BookService;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for BookService write-through cache consistency.
 *
 * Validates: Requirements 8.3, 8.4, 8.5
 */
@Label("Feature: entity-cache-abstraction, Property 8: Service write-through keeps cache consistent (BookService)")
class BookServiceWriteThroughPropertyTest {

    private final BookRepository bookRepository;
    private final AuthorService authorService;
    private final BookCache bookCache;
    private final BookService bookService;

    BookServiceWriteThroughPropertyTest() {
        this.bookRepository = mock(BookRepository.class);
        this.authorService = mock(AuthorService.class);

        // Use a real BookCache backed by a mocked repository for loadAll
        BookRepository cacheRepository = mock(BookRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        this.bookCache = new BookCache(cacheRepository);
        this.bookCache.init();

        this.bookService = new BookService(bookRepository, authorService, bookCache);
    }

    /**
     * Property 8: After addBook, the book is retrievable from the cache via findByIsbn.
     *
     * For any valid book, after addBook completes successfully, the cache contains
     * the book and it is retrievable by ISBN.
     *
     * Validates: Requirements 8.3
     */
    @Property(tries = 100)
    @Label("Property 8: After addBook, book is retrievable from cache via findByIsbn")
    void afterAddBookCacheContainsBook(@ForAll("validBooks") Book book) {
        // Setup: ISBN does not already exist
        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        bookService.addBook(book);

        // Assert: book is in cache
        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNotNull(cached, "Book should be retrievable from cache after addBook");
        assertEquals(book.getIsbn(), cached.getIsbn(), "Cached book ISBN should match");
        assertEquals(book.getTitle(), cached.getTitle(), "Cached book title should match");

        // Cleanup for next iteration
        bookCache.evict(book.getIsbn());
    }

    /**
     * Property 8: After updateBook, the updated book is retrievable from the cache.
     *
     * For any valid book that exists in the repository, after updateBook completes,
     * the cache reflects the updated values.
     *
     * Validates: Requirements 8.4
     */
    @Property(tries = 100)
    @Label("Property 8: After updateBook, updated book is retrievable from cache")
    void afterUpdateBookCacheReflectsUpdate(@ForAll("validBooks") Book book, @ForAll("validTitles") String newTitle) {
        // Setup: book exists in repository
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(book);
        Author author = book.getAuthor();
        when(authorService.getAuthorById(author.getId())).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Book updated = bookService.updateBook(book.getIsbn(), newTitle, author.getId(), book.getGenre(), book.getTotalCopies());

        // Assert: updated book is in cache
        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNotNull(cached, "Updated book should be retrievable from cache after updateBook");
        assertEquals(newTitle, cached.getTitle(), "Cached book title should reflect the update");
        assertEquals(book.getIsbn(), cached.getIsbn(), "Cached book ISBN should remain the same");

        // Cleanup for next iteration
        bookCache.evict(book.getIsbn());
    }

    /**
     * Property 8: After removeBook, the book is no longer in the cache.
     *
     * For any valid book that was previously added to the cache, after removeBook
     * completes, the book is not retrievable from the cache.
     *
     * Validates: Requirements 8.5
     */
    @Property(tries = 100)
    @Label("Property 8: After removeBook, book is no longer in cache")
    void afterRemoveBookCacheDoesNotContainBook(@ForAll("validBooks") Book book) {
        // Setup: put book in cache first
        bookCache.put(book);
        assertNotNull(bookService.findByIsbn(book.getIsbn()), "Precondition: book should be in cache before removal");

        // Mock repository delete to succeed
        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(true);
        doNothing().when(bookRepository).deleteByIsbn(book.getIsbn());

        // Act
        bookService.removeBook(book.getIsbn());

        // Assert: book is no longer in cache
        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNull(cached, "Book should not be retrievable from cache after removeBook");
    }

    // --- Generators ---

    @Provide
    Arbitrary<Book> validBooks() {
        Arbitrary<String> isbns = Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(20);
        Arbitrary<String> titles = validTitles();
        Arbitrary<String> genres = Arbitraries.of("Fiction", "Non-Fiction", "Science", "History", "Fantasy", "Mystery");
        Arbitrary<Integer> totalCopies = Arbitraries.integers().between(1, 100);
        Arbitrary<Author> authors = Arbitraries.longs().between(1L, 10000L)
                .flatMap(id -> Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
                        .map(name -> Author.builder().id(id).name(name).deleted(false).build()));

        return Combinators.combine(isbns, titles, authors, genres, totalCopies)
                .as((isbn, title, author, genre, copies) ->
                        Book.builder()
                                .isbn(isbn)
                                .title(title)
                                .author(author)
                                .genre(genre)
                                .totalCopies(copies)
                                .availableCopies(copies)
                                .build()
                );
    }

    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100);
    }
}

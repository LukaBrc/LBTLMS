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

class BookServiceWriteThroughPropertyTest {

    private final BookRepository bookRepository;
    private final AuthorService authorService;
    private final BookCache bookCache;
    private final BookService bookService;

    BookServiceWriteThroughPropertyTest() {
        this.bookRepository = mock(BookRepository.class);
        this.authorService = mock(AuthorService.class);

        BookRepository cacheRepository = mock(BookRepository.class);
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        this.bookCache = new BookCache(cacheRepository);
        this.bookCache.init();

        this.bookService = new BookService(bookRepository, authorService, bookCache);
    }

    @Property(tries = 100)
    void afterAddBookCacheContainsBook(@ForAll("validBooks") Book book) {
        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookService.addBook(book);

        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNotNull(cached, "Book should be retrievable from cache after addBook");
        assertEquals(book.getIsbn(), cached.getIsbn(), "Cached book ISBN should match");
        assertEquals(book.getTitle(), cached.getTitle(), "Cached book title should match");

        bookCache.evict(book.getIsbn());
    }

    @Property(tries = 100)
    void afterUpdateBookCacheReflectsUpdate(@ForAll("validBooks") Book book, @ForAll("validTitles") String newTitle) {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(book);
        Author author = book.getAuthor();
        when(authorService.getAuthorById(author.getId())).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updated = bookService.updateBook(book.getIsbn(), newTitle, author.getId(), book.getGenre(), book.getTotalCopies());

        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNotNull(cached, "Updated book should be retrievable from cache after updateBook");
        assertEquals(newTitle, cached.getTitle(), "Cached book title should reflect the update");
        assertEquals(book.getIsbn(), cached.getIsbn(), "Cached book ISBN should remain the same");

        bookCache.evict(book.getIsbn());
    }

    @Property(tries = 100)
    void afterRemoveBookCacheDoesNotContainBook(@ForAll("validBooks") Book book) {
        bookCache.put(book);
        assertNotNull(bookService.findByIsbn(book.getIsbn()), "Precondition: book should be in cache before removal");

        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(true);
        doNothing().when(bookRepository).deleteByIsbn(book.getIsbn());

        bookService.removeBook(book.getIsbn());

        Book cached = bookService.findByIsbn(book.getIsbn());
        assertNull(cached, "Book should not be retrievable from cache after removeBook");
    }


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

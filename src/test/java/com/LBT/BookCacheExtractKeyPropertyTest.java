package com.lbt;

import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.services.BookCache;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for BookCache.extractKey consistency.
 *
 * Validates: Requirements 6.3
 */
@Label("Feature: entity-cache-abstraction, Property 7: BookCache extractKey consistency")
class BookCacheExtractKeyPropertyTest {

    private final BookCache bookCache;

    BookCacheExtractKeyPropertyTest() {
        BookRepository mockRepository = mock(BookRepository.class);
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());
        this.bookCache = new BookCache(mockRepository);
    }

    /**
     * Property 7: ExtractKey consistency — BookCache.extractKey(book) == book.getIsbn()
     *
     * For any Book entity with a non-null isbn, BookCache.extractKey(book)
     * should return book.getIsbn().
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 200)
    @Label("Property 7: BookCache.extractKey(book) == book.getIsbn()")
    void extractKeyReturnsBookIsbn(@ForAll("booksWithNonNullIsbn") Book book) {
        String expectedKey = book.getIsbn();

        // Use put + getById to verify extractKey behavior indirectly
        // Since extractKey is protected, we verify it through the cache's public API:
        // put(book) uses extractKey internally, and getById(expectedKey) should find it.
        bookCache.put(book);

        assertTrue(bookCache.getById(expectedKey).isPresent(),
                "getById(book.getIsbn()) should find the book after put — confirms extractKey returns getIsbn()");
        assertEquals(book, bookCache.getById(expectedKey).get(),
                "The retrieved book should be the same instance that was put");

        // Clean up for next iteration
        bookCache.evict(expectedKey);
    }

    // --- Generator ---

    @Provide
    Arbitrary<Book> booksWithNonNullIsbn() {
        Arbitrary<String> isbns = Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> titles = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, Long.MAX_VALUE);

        return Combinators.combine(ids, isbns, titles).as((id, isbn, title) ->
                Book.builder().id(id).isbn(isbn).title(title).build()
        );
    }
}

package com.lbt;

import com.lbt.entities.Book;
import com.lbt.repositories.BookRepository;
import com.lbt.services.BookCache;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCacheExtractKeyPropertyTest {

    private final BookCache bookCache;

    BookCacheExtractKeyPropertyTest() {
        BookRepository mockRepository = mock(BookRepository.class);
        when(mockRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());
        this.bookCache = new BookCache(mockRepository);
    }

    @Property(tries = 200)
    void extractKeyReturnsBookIsbn(@ForAll("booksWithNonNullIsbn") Book book) {
        String expectedKey = book.getIsbn();

        bookCache.put(book);

        assertTrue(bookCache.getById(expectedKey).isPresent(),
                "getById(book.getIsbn()) should find the book after put - confirms extractKey returns getIsbn()");
        assertEquals(book, bookCache.getById(expectedKey).get(),
                "The retrieved book should be the same instance that was put");

        bookCache.evict(expectedKey);
    }


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

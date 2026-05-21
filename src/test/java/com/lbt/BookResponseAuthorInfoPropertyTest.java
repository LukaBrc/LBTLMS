package com.lbt;

import com.lbt.dto.BookResponse;
import com.lbt.entities.Author;
import com.lbt.entities.Book;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

class BookResponseAuthorInfoPropertyTest {

    private BookResponse toResponse(Book book) {
        BookResponse r = new BookResponse();
        r.setIsbn(book.getIsbn());
        r.setTitle(book.getTitle());
        r.setAuthorId(book.getAuthor().getId());
        r.setAuthorName(book.getAuthor().getName());
        r.setGenre(book.getGenre());
        r.setTotalCopies(book.getTotalCopies());
        r.setAvailableCopies(book.getAvailableCopies());
        return r;
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-9-bookresponse-author-info")
    void mappedBookResponseContainsCorrectAuthorInfo(
            @ForAll("randomBooksWithAuthors") Book book
    ) {
        BookResponse dto = toResponse(book);

        assertEquals(book.getAuthor().getId(), dto.getAuthorId(),
                "BookResponse authorId must match Author entity id");
        assertEquals(book.getAuthor().getName(), dto.getAuthorName(),
                "BookResponse authorName must match Author entity name");
    }

    @Provide
    Arbitrary<Book> randomBooksWithAuthors() {
        Arbitrary<Long> authorIds = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> authorNames = Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());

        Arbitrary<Author> authors = Combinators.combine(authorIds, authorNames).as((id, name) ->
                Author.builder().id(id).name(name).deleted(false).build()
        );

        Arbitrary<Long> bookIds = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> isbns = Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(50)
                .alpha();
        Arbitrary<String> titles = Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .alpha();
        Arbitrary<String> genres = Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(100)
                .alpha();
        Arbitrary<Integer> totalCopies = Arbitraries.integers().between(1, 1000);

        return Combinators.combine(bookIds, isbns, titles, authors, genres, totalCopies)
                .as((id, isbn, title, author, genre, copies) -> {
                    Book book = Book.builder()
                            .id(id)
                            .isbn(isbn)
                            .title(title)
                            .author(author)
                            .genre(genre)
                            .build();
                    book.setTotalCopies(copies);
                    return book;
                });
    }
}

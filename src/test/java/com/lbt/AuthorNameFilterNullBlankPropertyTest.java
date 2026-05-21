package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;

import net.jqwik.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
class AuthorNameFilterNullBlankPropertyTest {

    @Property(tries = 100)
    void nullOrBlankFilterReturnsAllAuthors(
            @ForAll("randomAuthorLists") List<Author> authors,
            @ForAll("nullOrBlankStrings") String filter) {

        AuthorRepository mockRepository = mock(AuthorRepository.class);
        when(mockRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        AuthorCache authorCache = new AuthorCache(mockRepository);

        for (Author author : authors) {
            authorCache.put(author);
        }

        AuthorService authorService = new AuthorService(mockRepository, authorCache);

        List<Author> result = authorService.searchAuthorsByNameContains(filter);

        assertEquals(authors.size(), result.size(),
                "Result size must equal the full author list size when filter is null/blank");
        assertTrue(result.containsAll(authors),
                "Result must contain all authors from the cache when filter is null/blank");
        assertTrue(authors.containsAll(result),
                "Result must not contain any extra authors beyond what is in the cache");
    }


    @Provide
    public Arbitrary<List<Author>> randomAuthorLists() {
        Arbitrary<Author> authorArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 100_000L),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
        ).as((id, name) -> Author.builder().id(id).name(name).deleted(false).build());

        return authorArbitrary.list().ofMinSize(0).ofMaxSize(20).uniqueElements(Author::getId);
    }

    @Provide
    public Arbitrary<String> nullOrBlankStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.strings()
                        .whitespace()
                        .ofMinLength(1)
                        .ofMaxLength(10)
        );
    }
}

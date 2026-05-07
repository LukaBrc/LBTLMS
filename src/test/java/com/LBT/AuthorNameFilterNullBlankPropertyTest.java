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

/**
 * Property 1: Null or blank filter returns all authors
 *
 * For any list of authors in the cache and for any null or blank (whitespace-only)
 * filter string, calling getAuthorsByName SHALL return the complete unfiltered list.
 *
 * Validates: Requirements 1.2, 1.3
 */
@Label("Feature: author-name-filter, Property 1: Null or blank filter returns all authors")
class AuthorNameFilterNullBlankPropertyTest {

    /**
     * Property 1: Null or blank filter returns all authors.
     *
     * For any random list of authors and any null/blank filter string,
     * getAuthorsByName must return the full list unchanged.
     *
     * Validates: Requirements 1.2, 1.3
     */
    @Property(tries = 100)
    @Label("Null or blank filter returns all authors unchanged")
    void nullOrBlankFilterReturnsAllAuthors(
            @ForAll("randomAuthorLists") List<Author> authors,
            @ForAll("nullOrBlankStrings") String filter) {

        // Set up a mocked AuthorCache that returns our generated author list
        AuthorRepository mockRepository = mock(AuthorRepository.class);
        when(mockRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        AuthorCache authorCache = new AuthorCache(mockRepository);

        // Populate the cache with generated authors
        for (Author author : authors) {
            authorCache.put(author);
        }

        AuthorService authorService = new AuthorService(mockRepository, authorCache);

        // Act
        List<Author> result = authorService.getAuthorsByName(filter);

        // Assert: result must contain exactly the same authors as the input list
        assertEquals(authors.size(), result.size(),
                "Result size must equal the full author list size when filter is null/blank");
        assertTrue(result.containsAll(authors),
                "Result must contain all authors from the cache when filter is null/blank");
        assertTrue(authors.containsAll(result),
                "Result must not contain any extra authors beyond what is in the cache");
    }

    // --- Generators ---

    @Provide
    Arbitrary<List<Author>> randomAuthorLists() {
        Arbitrary<Author> authorArbitrary = Combinators.combine(
                Arbitraries.longs().between(1L, 100_000L),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
        ).as((id, name) -> Author.builder().id(id).name(name).deleted(false).build());

        return authorArbitrary.list().ofMinSize(0).ofMaxSize(20).uniqueElements(Author::getId);
    }

    @Provide
    Arbitrary<String> nullOrBlankStrings() {
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

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
 * Property 2: Filter correctness (inclusion and exclusion)
 *
 * For any list of authors in the cache and for any non-blank filter string,
 * calling getAuthorsByName SHALL return exactly those authors whose name contains
 * the filter string using case-insensitive comparison — every returned author's
 * name contains the filter (case-insensitive), and every excluded author's name
 * does NOT contain the filter (case-insensitive).
 *
 * Validates: Requirements 2.1, 2.3, 3.2
 */
@Label("Feature: author-name-filter, Property 2: Filter correctness (inclusion and exclusion)")
class AuthorNameFilterCorrectnessPropertyTest {

    /**
     * Property 2: Filter correctness (inclusion and exclusion).
     *
     * For any random list of authors and any non-blank filter string,
     * getAuthorsByName must return exactly those authors whose name contains
     * the filter (case-insensitive), and exclude all others.
     *
     * Validates: Requirements 2.1, 2.3, 3.2
     */
    @Property(tries = 100)
    @Label("Non-blank filter returns exactly matching authors and excludes non-matching")
    void filterReturnsExactlyMatchingAuthors(
            @ForAll("randomAuthorLists") List<Author> authors,
            @ForAll("nonBlankStrings") String filter) {

        // Set up a mocked AuthorRepository and real AuthorCache
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

        // Determine excluded authors (those in the input but not in the result)
        List<Author> excluded = authors.stream()
                .filter(a -> !result.contains(a))
                .toList();

        // Assert: every returned author's name contains the filter (case-insensitive)
        String lowerFilter = filter.toLowerCase();
        for (Author author : result) {
            assertTrue(author.getName().toLowerCase().contains(lowerFilter),
                    String.format("Returned author '%s' does not contain filter '%s' (case-insensitive)",
                            author.getName(), filter));
        }

        // Assert: every excluded author's name does NOT contain the filter (case-insensitive)
        for (Author author : excluded) {
            assertFalse(author.getName().toLowerCase().contains(lowerFilter),
                    String.format("Excluded author '%s' contains filter '%s' (case-insensitive) but was not returned",
                            author.getName(), filter));
        }
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
    Arbitrary<String> nonBlankStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
    }
}

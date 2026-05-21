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

class AuthorNameFilterCorrectnessPropertyTest {

    @Property(tries = 100)
    void filterReturnsExactlyMatchingAuthors(
            @ForAll("randomAuthorLists") List<Author> authors,
            @ForAll("nonBlankStrings") String filter) {

        AuthorRepository mockRepository = mock(AuthorRepository.class);
        when(mockRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        AuthorCache authorCache = new AuthorCache(mockRepository);

        for (Author author : authors) {
            authorCache.put(author);
        }

        AuthorService authorService = new AuthorService(mockRepository, authorCache);

        List<Author> result = authorService.getAuthorsByName(filter);

        List<Author> excluded = authors.stream()
                .filter(a -> !result.contains(a))
                .toList();

        String lowerFilter = filter.toLowerCase();
        for (Author author : result) {
            assertTrue(author.getName().toLowerCase().contains(lowerFilter),
                    String.format("Returned author '%s' does not contain filter '%s' (case-insensitive)",
                            author.getName(), filter));
        }

        for (Author author : excluded) {
            assertFalse(author.getName().toLowerCase().contains(lowerFilter),
                    String.format("Excluded author '%s' contains filter '%s' (case-insensitive) but was not returned",
                            author.getName(), filter));
        }
    }


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

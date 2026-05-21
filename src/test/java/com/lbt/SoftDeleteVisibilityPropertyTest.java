package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SoftDeleteVisibilityPropertyTest {

    @Property(tries = 100)
    @Tag("feature-author-management-property-4-soft-delete-visibility-invariant")
    void getAllAuthorsReturnsOnlyActiveAuthors(@ForAll("authorLists") List<Author> allAuthors) {
        List<Author> activeAuthors = allAuthors.stream()
                .filter(a -> !a.isDeleted())
                .collect(Collectors.toList());

        AuthorCache cache = mock(AuthorCache.class);
        when(cache.getAll()).thenReturn(activeAuthors);

        AuthorRepository repo = mock(AuthorRepository.class);

        AuthorService service = new AuthorService(repo, cache);

        List<Author> result = service.getAllAuthors();

        assertEquals(activeAuthors.size(), result.size(),
                "getAllAuthors should return exactly the number of active authors");
        assertTrue(result.containsAll(activeAuthors),
                "getAllAuthors should contain all active authors");
        assertTrue(result.stream().noneMatch(Author::isDeleted),
                "getAllAuthors should not contain any deleted authors");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-4-soft-delete-visibility-invariant")
    void getAuthorByIdThrowsForNonExistentId(@ForAll @LongRange(min = 1, max = 100000) Long nonExistentId) {
        AuthorCache cache = mock(AuthorCache.class);
        when(cache.getById(nonExistentId)).thenReturn(Optional.empty());

        AuthorRepository repo = mock(AuthorRepository.class);

        AuthorService service = new AuthorService(repo, cache);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getAuthorById(nonExistentId),
                "getAuthorById should throw IllegalArgumentException for non-existent id");
        assertTrue(ex.getMessage().contains(String.valueOf(nonExistentId)),
                "Exception message should contain the requested id");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-4-soft-delete-visibility-invariant")
    void updateAuthorThrowsForSoftDeletedAuthor(
            @ForAll @LongRange(min = 1, max = 100000) Long deletedAuthorId,
            @ForAll("validNames") String newName) {
        AuthorRepository repo = mock(AuthorRepository.class);
        when(repo.findByIdAndDeletedFalse(deletedAuthorId)).thenReturn(Optional.empty());

        AuthorCache cache = mock(AuthorCache.class);

        AuthorService service = new AuthorService(repo, cache);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateAuthor(deletedAuthorId, newName),
                "updateAuthor should throw IllegalArgumentException for soft-deleted author");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-4-soft-delete-visibility-invariant")
    void deleteAuthorThrowsForSoftDeletedAuthor(
            @ForAll @LongRange(min = 1, max = 100000) Long deletedAuthorId) {
        AuthorRepository repo = mock(AuthorRepository.class);
        when(repo.findByIdAndDeletedFalse(deletedAuthorId)).thenReturn(Optional.empty());

        AuthorCache cache = mock(AuthorCache.class);

        AuthorService service = new AuthorService(repo, cache);

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAuthor(deletedAuthorId),
                "deleteAuthor should throw IllegalArgumentException for soft-deleted author");
    }

    @Provide
    Arbitrary<List<Author>> authorLists() {
        return Arbitraries.integers().between(0, 20).flatMap(size ->
                Arbitraries.integers().between(0, size).flatMap(deletedCount -> {
                    int activeCount = size - deletedCount;
                    Arbitrary<Author> activeAuthor = validNames().map(name -> {
                        Author a = Author.builder().name(name).deleted(false).build();
                        a.setId((long) (Math.abs(name.hashCode()) % 100000 + 1));
                        return a;
                    });
                    Arbitrary<Author> deletedAuthor = validNames().map(name -> {
                        Author a = Author.builder().name(name).deleted(true).build();
                        a.setId((long) (Math.abs(name.hashCode()) % 100000 + 100001));
                        return a;
                    });
                    return Combinators.combine(
                            activeAuthor.list().ofSize(activeCount),
                            deletedAuthor.list().ofSize(deletedCount)
                    ).as((actives, deleteds) -> {
                        actives.addAll(deleteds);
                        return actives;
                    });
                })
        );
    }

    @Provide
    Arbitrary<String> validNames() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

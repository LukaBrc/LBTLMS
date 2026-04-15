package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property 4: Soft-delete visibility invariant
 *
 * For any set of authors with varying deleted flags, the service's read operations
 * return exactly the authors where deleted is false — no active author is omitted,
 * no deleted or non-existent author is included. Attempting to read, update, or delete
 * a soft-deleted or non-existent author signals not-found.
 *
 * **Validates: Requirements 4.1, 4.2, 4.3, 5.2, 6.2, 6.3**
 */
@Label("Feature: author-management, Property 4: Soft-delete visibility invariant")
class SoftDeleteVisibilityPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 4: Soft-delete visibility invariant")
    @Label("getAllAuthors returns exactly the active (non-deleted) authors from cache")
    void getAllAuthorsReturnsOnlyActiveAuthors(@ForAll("authorLists") List<Author> allAuthors) {
        // Separate active vs deleted
        List<Author> activeAuthors = allAuthors.stream()
                .filter(a -> !a.isDeleted())
                .collect(Collectors.toList());

        // Build a cache mock that returns only active authors (as the real cache does)
        AuthorCache cache = mock(AuthorCache.class);
        when(cache.getAll()).thenReturn(activeAuthors);

        AuthorRepository repo = mock(AuthorRepository.class);
        ValidationHandler validationHandler = new ValidationHandler();

        AuthorService service = new AuthorService(repo, cache, validationHandler);

        List<Author> result = service.getAllAuthors();

        // Result should contain exactly the active authors
        assertEquals(activeAuthors.size(), result.size(),
                "getAllAuthors should return exactly the number of active authors");
        assertTrue(result.containsAll(activeAuthors),
                "getAllAuthors should contain all active authors");
        assertTrue(result.stream().noneMatch(Author::isDeleted),
                "getAllAuthors should not contain any deleted authors");
    }

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 4: Soft-delete visibility invariant")
    @Label("getAuthorById throws IllegalArgumentException for non-existent id")
    void getAuthorByIdThrowsForNonExistentId(@ForAll @LongRange(min = 1, max = 100000) Long nonExistentId) {
        AuthorCache cache = mock(AuthorCache.class);
        when(cache.getById(nonExistentId)).thenReturn(Optional.empty());

        AuthorRepository repo = mock(AuthorRepository.class);
        ValidationHandler validationHandler = new ValidationHandler();

        AuthorService service = new AuthorService(repo, cache, validationHandler);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getAuthorById(nonExistentId),
                "getAuthorById should throw IllegalArgumentException for non-existent id");
        assertTrue(ex.getMessage().contains(String.valueOf(nonExistentId)),
                "Exception message should contain the requested id");
    }

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 4: Soft-delete visibility invariant")
    @Label("updateAuthor throws IllegalArgumentException for soft-deleted author")
    void updateAuthorThrowsForSoftDeletedAuthor(
            @ForAll @LongRange(min = 1, max = 100000) Long deletedAuthorId,
            @ForAll("validNames") String newName) {
        // Repository returns empty for soft-deleted author
        AuthorRepository repo = mock(AuthorRepository.class);
        when(repo.findByIdAndDeletedFalse(deletedAuthorId)).thenReturn(Optional.empty());

        AuthorCache cache = mock(AuthorCache.class);
        ValidationHandler validationHandler = new ValidationHandler();

        AuthorService service = new AuthorService(repo, cache, validationHandler);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateAuthor(deletedAuthorId, newName),
                "updateAuthor should throw IllegalArgumentException for soft-deleted author");
    }

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 4: Soft-delete visibility invariant")
    @Label("deleteAuthor throws IllegalArgumentException for soft-deleted author")
    void deleteAuthorThrowsForSoftDeletedAuthor(
            @ForAll @LongRange(min = 1, max = 100000) Long deletedAuthorId) {
        // Repository returns empty for soft-deleted author
        AuthorRepository repo = mock(AuthorRepository.class);
        when(repo.findByIdAndDeletedFalse(deletedAuthorId)).thenReturn(Optional.empty());

        AuthorCache cache = mock(AuthorCache.class);
        ValidationHandler validationHandler = new ValidationHandler();

        AuthorService service = new AuthorService(repo, cache, validationHandler);

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

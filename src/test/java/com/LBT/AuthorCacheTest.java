package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorCache.
 * Validates: Requirements 7.1, 7.8
 */
class AuthorCacheTest {

    private AuthorRepository authorRepository;
    private AuthorCache authorCache;

    @BeforeEach
    void setUp() {
        authorRepository = mock(AuthorRepository.class);
        when(authorRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        authorCache = new AuthorCache(authorRepository);
    }

    // --- init() tests (Requirement 7.1) ---

    @Test
    @DisplayName("init() loads all active authors from findByDeletedFalse into the cache")
    void initLoadsActiveAuthorsIntoCache() {
        Author a1 = Author.builder().id(1L).name("Author One").deleted(false).build();
        Author a2 = Author.builder().id(2L).name("Author Two").deleted(false).build();
        when(authorRepository.findByDeletedFalse()).thenReturn(List.of(a1, a2));

        authorCache.init();

        List<Author> all = authorCache.getAll();
        assertEquals(2, all.size());
        assertTrue(authorCache.getById(1L).isPresent());
        assertTrue(authorCache.getById(2L).isPresent());
        assertEquals("Author One", authorCache.getById(1L).get().getName());
        assertEquals("Author Two", authorCache.getById(2L).get().getName());
    }

    @Test
    @DisplayName("init() with no active authors results in empty cache")
    void initWithNoAuthorsResultsInEmptyCache() {
        when(authorRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());

        authorCache.init();

        assertTrue(authorCache.getAll().isEmpty());
    }

    // --- refresh() resilience tests (Requirement 7.8) ---

    @Test
    @DisplayName("refresh() retains stale data when repository throws an exception")
    void refreshRetainsStaleDataOnException() {
        // Load initial data
        Author a1 = Author.builder().id(1L).name("Stale Author").deleted(false).build();
        when(authorRepository.findByDeletedFalse()).thenReturn(List.of(a1));
        authorCache.init();

        // Now make the repository throw on the next call
        when(authorRepository.findByDeletedFalse()).thenThrow(new RuntimeException("DB unreachable"));

        authorCache.refresh();

        // Cache should still have the stale data
        assertEquals(1, authorCache.getAll().size());
        assertTrue(authorCache.getById(1L).isPresent());
        assertEquals("Stale Author", authorCache.getById(1L).get().getName());
    }

    @Test
    @DisplayName("refresh() replaces cache contents when repository succeeds")
    void refreshReplacesCacheOnSuccess() {
        // Load initial data
        Author a1 = Author.builder().id(1L).name("Old Author").deleted(false).build();
        when(authorRepository.findByDeletedFalse()).thenReturn(List.of(a1));
        authorCache.init();

        // Refresh with new data
        Author a2 = Author.builder().id(2L).name("New Author").deleted(false).build();
        when(authorRepository.findByDeletedFalse()).thenReturn(List.of(a2));

        authorCache.refresh();

        assertEquals(1, authorCache.getAll().size());
        assertFalse(authorCache.getById(1L).isPresent(), "Old author should be gone after refresh");
        assertTrue(authorCache.getById(2L).isPresent());
        assertEquals("New Author", authorCache.getById(2L).get().getName());
    }

    // --- put / evict / getAll / getById tests ---

    @Test
    @DisplayName("put() adds an author to the cache, retrievable by getById and getAll")
    void putAddsAuthorToCache() {
        authorCache.init();

        Author author = Author.builder().id(10L).name("New Entry").deleted(false).build();
        authorCache.put(author);

        Optional<Author> found = authorCache.getById(10L);
        assertTrue(found.isPresent());
        assertEquals("New Entry", found.get().getName());
        assertEquals(1, authorCache.getAll().size());
    }

    @Test
    @DisplayName("put() overwrites an existing author with the same id")
    void putOverwritesExistingAuthor() {
        authorCache.init();

        Author original = Author.builder().id(5L).name("Original").deleted(false).build();
        authorCache.put(original);

        Author updated = Author.builder().id(5L).name("Updated").deleted(false).build();
        authorCache.put(updated);

        assertEquals(1, authorCache.getAll().size());
        assertEquals("Updated", authorCache.getById(5L).get().getName());
    }

    @Test
    @DisplayName("evict() removes an author from the cache")
    void evictRemovesAuthorFromCache() {
        authorCache.init();

        Author author = Author.builder().id(7L).name("To Remove").deleted(false).build();
        authorCache.put(author);
        assertTrue(authorCache.getById(7L).isPresent());

        authorCache.evict(7L);

        assertFalse(authorCache.getById(7L).isPresent());
        assertTrue(authorCache.getAll().isEmpty());
    }

    @Test
    @DisplayName("evict() on non-existent id does not throw")
    void evictNonExistentIdDoesNotThrow() {
        authorCache.init();
        assertDoesNotThrow(() -> authorCache.evict(999L));
    }

    @Test
    @DisplayName("getById() returns empty Optional for missing id")
    void getByIdReturnsEmptyForMissingId() {
        authorCache.init();
        assertTrue(authorCache.getById(42L).isEmpty());
    }

    @Test
    @DisplayName("getAll() returns all cached authors")
    void getAllReturnsAllCachedAuthors() {
        authorCache.init();

        authorCache.put(Author.builder().id(1L).name("A").deleted(false).build());
        authorCache.put(Author.builder().id(2L).name("B").deleted(false).build());
        authorCache.put(Author.builder().id(3L).name("C").deleted(false).build());

        List<Author> all = authorCache.getAll();
        assertEquals(3, all.size());
    }
}

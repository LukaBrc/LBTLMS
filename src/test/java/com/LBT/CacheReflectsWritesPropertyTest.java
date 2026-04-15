package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;

import net.jqwik.api.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property 7: Cache reflects writes immediately
 *
 * For any sequence of create/update/soft-delete operations, the AuthorCache
 * reflects each change immediately after the operation completes.
 *
 * Validates: Requirements 7.7
 */
@Label("Feature: author-management, Property 7: Cache reflects writes immediately")
class CacheReflectsWritesPropertyTest {

    private AuthorCache createEmptyCache() {
        AuthorRepository mockRepo = mock(AuthorRepository.class);
        when(mockRepo.findByDeletedFalse()).thenReturn(Collections.emptyList());
        AuthorCache cache = new AuthorCache(mockRepo);
        cache.init();
        return cache;
    }

    /**
     * After put(), the author is immediately visible via getById() and present in getAll().
     *
     * Validates: Requirements 7.7
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 7: Cache reflects writes immediately")
    @Label("After put, author is immediately visible via getById and getAll")
    void afterPutAuthorIsImmediatelyVisible(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String name) {

        AuthorCache cache = createEmptyCache();

        Author author = Author.builder().id(id).name(name).deleted(false).build();
        cache.put(author);

        // getById must return the author immediately
        Optional<Author> found = cache.getById(id);
        assertTrue(found.isPresent(), "Author should be present in cache after put()");
        assertEquals(name, found.get().getName(), "Cached author name should match");
        assertEquals(id, found.get().getId(), "Cached author id should match");

        // getAll must contain the author
        List<Author> all = cache.getAll();
        assertTrue(all.stream().anyMatch(a -> a.getId().equals(id) && a.getName().equals(name)),
                "getAll() should contain the author after put()");
    }

    /**
     * After evict(), the author is immediately absent from getById() and getAll().
     *
     * Validates: Requirements 7.7
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 7: Cache reflects writes immediately")
    @Label("After evict, author is immediately absent from getById and getAll")
    void afterEvictAuthorIsImmediatelyAbsent(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String name) {

        AuthorCache cache = createEmptyCache();

        // First put the author in the cache
        Author author = Author.builder().id(id).name(name).deleted(false).build();
        cache.put(author);
        assertTrue(cache.getById(id).isPresent(), "Precondition: author should be in cache");

        // Evict and verify immediate absence
        cache.evict(id);

        Optional<Author> found = cache.getById(id);
        assertFalse(found.isPresent(), "Author should be absent from cache after evict()");

        List<Author> all = cache.getAll();
        assertFalse(all.stream().anyMatch(a -> a.getId().equals(id)),
                "getAll() should not contain the author after evict()");
    }

    /**
     * After put() with an updated name, getById() returns the updated author.
     *
     * Validates: Requirements 7.7
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 7: Cache reflects writes immediately")
    @Label("After put with updated name, getById returns updated author")
    void afterPutWithUpdatedNameCacheReflectsUpdate(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String originalName,
            @ForAll("authorNames") String updatedName) {

        AuthorCache cache = createEmptyCache();

        // Put original author
        Author original = Author.builder().id(id).name(originalName).deleted(false).build();
        cache.put(original);

        // Update with new name via put
        Author updated = Author.builder().id(id).name(updatedName).deleted(false).build();
        cache.put(updated);

        // getById must return the updated author
        Optional<Author> found = cache.getById(id);
        assertTrue(found.isPresent(), "Author should be present after update put()");
        assertEquals(updatedName, found.get().getName(),
                "Cached author name should reflect the updated value");
        assertEquals(id, found.get().getId(), "Author id should remain unchanged");
    }

    @Provide
    Arbitrary<Long> authorIds() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<String> authorNames() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

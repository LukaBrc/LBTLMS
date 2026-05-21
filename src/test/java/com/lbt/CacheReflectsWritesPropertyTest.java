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

class CacheReflectsWritesPropertyTest {

    private AuthorCache createEmptyCache() {
        AuthorRepository mockRepo = mock(AuthorRepository.class);
        when(mockRepo.findByDeletedFalse()).thenReturn(Collections.emptyList());
        AuthorCache cache = new AuthorCache(mockRepo);
        cache.init();
        return cache;
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-7-cache-reflects-writes-immediately")
    void afterPutAuthorIsImmediatelyVisible(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String name) {

        AuthorCache cache = createEmptyCache();

        Author author = Author.builder().id(id).name(name).deleted(false).build();
        cache.put(author);

        Optional<Author> found = cache.getById(id);
        assertTrue(found.isPresent(), "Author should be present in cache after put()");
        assertEquals(name, found.get().getName(), "Cached author name should match");
        assertEquals(id, found.get().getId(), "Cached author id should match");

        List<Author> all = cache.getAll();
        assertTrue(all.stream().anyMatch(a -> a.getId().equals(id) && a.getName().equals(name)),
                "getAll() should contain the author after put()");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-7-cache-reflects-writes-immediately")
    void afterEvictAuthorIsImmediatelyAbsent(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String name) {

        AuthorCache cache = createEmptyCache();

        Author author = Author.builder().id(id).name(name).deleted(false).build();
        cache.put(author);
        assertTrue(cache.getById(id).isPresent(), "Precondition: author should be in cache");

        cache.evict(id);

        Optional<Author> found = cache.getById(id);
        assertFalse(found.isPresent(), "Author should be absent from cache after evict()");

        List<Author> all = cache.getAll();
        assertFalse(all.stream().anyMatch(a -> a.getId().equals(id)),
                "getAll() should not contain the author after evict()");
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-7-cache-reflects-writes-immediately")
    void afterPutWithUpdatedNameCacheReflectsUpdate(
            @ForAll("authorIds") Long id,
            @ForAll("authorNames") String originalName,
            @ForAll("authorNames") String updatedName) {

        AuthorCache cache = createEmptyCache();

        Author original = Author.builder().id(id).name(originalName).deleted(false).build();
        cache.put(original);

        Author updated = Author.builder().id(id).name(updatedName).deleted(false).build();
        cache.put(updated);

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

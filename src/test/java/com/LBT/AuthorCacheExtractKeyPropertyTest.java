package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for AuthorCache.extractKey consistency.
 *
 * Validates: Requirements 5.3
 */
@Label("Feature: entity-cache-abstraction, Property 7: AuthorCache extractKey consistency")
class AuthorCacheExtractKeyPropertyTest {

    private final AuthorCache authorCache;

    AuthorCacheExtractKeyPropertyTest() {
        AuthorRepository mockRepository = mock(AuthorRepository.class);
        when(mockRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        this.authorCache = new AuthorCache(mockRepository);
    }

    /**
     * Property 7: ExtractKey consistency — AuthorCache.extractKey(author) == author.getId()
     *
     * For any Author entity with a non-null id, AuthorCache.extractKey(author)
     * should return author.getId().
     *
     * Validates: Requirements 5.3
     */
    @Property(tries = 200)
    @Label("Property 7: AuthorCache.extractKey(author) == author.getId()")
    void extractKeyReturnsAuthorId(@ForAll("authorsWithNonNullId") Author author) {
        Long expectedKey = author.getId();

        // Use put + getById to verify extractKey behavior indirectly
        // Since extractKey is protected, we verify it through the cache's public API:
        // put(author) uses extractKey internally, and getById(expectedKey) should find it.
        authorCache.put(author);

        assertTrue(authorCache.getById(expectedKey).isPresent(),
                "getById(author.getId()) should find the author after put — confirms extractKey returns getId()");
        assertEquals(author, authorCache.getById(expectedKey).get(),
                "The retrieved author should be the same instance that was put");

        // Clean up for next iteration
        authorCache.evict(expectedKey);
    }

    // --- Generator ---

    @Provide
    Arbitrary<Author> authorsWithNonNullId() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);

        return Combinators.combine(ids, names).as((id, name) ->
                Author.builder().id(id).name(name).deleted(false).build()
        );
    }
}

package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;

import net.jqwik.api.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorCacheExtractKeyPropertyTest {

    private final AuthorCache authorCache;

    AuthorCacheExtractKeyPropertyTest() {
        AuthorRepository mockRepository = mock(AuthorRepository.class);
        when(mockRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
        this.authorCache = new AuthorCache(mockRepository);
    }

    @Property(tries = 200)
    void extractKeyReturnsAuthorId(@ForAll("authorsWithNonNullId") Author author) {
        Long expectedKey = author.getId();

        authorCache.put(author);

        assertTrue(authorCache.getById(expectedKey).isPresent(),
                "getById(author.getId()) should find the author after put - confirms extractKey returns getId()");
        assertEquals(author, authorCache.getById(expectedKey).get(),
                "The retrieved author should be the same instance that was put");

        authorCache.evict(expectedKey);
    }

    @Provide
    Arbitrary<Author> authorsWithNonNullId() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);

        return Combinators.combine(ids, names).as((id, name) ->
                Author.builder().id(id).name(name).deleted(false).build()
        );
    }
}

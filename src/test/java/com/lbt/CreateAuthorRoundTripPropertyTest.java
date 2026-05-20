package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;

import net.jqwik.api.*;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CreateAuthorRoundTripPropertyTest {

    private final AtomicLong idCounter = new AtomicLong(1L);

    @Property(tries = 100)
    @Tag("feature-author-management-property-3-create-author-round-trip")
    void createAuthorRoundTrip(@ForAll("validAuthorNames") String name) {
        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);

        when(repo.save(any(Author.class))).thenAnswer(invocation -> {
            Author a = invocation.getArgument(0);
            a.setId(idCounter.getAndIncrement());
            return a;
        });

        AuthorService authorService = new AuthorService(repo, cache);

        Author result = authorService.createAuthor(name);

        assertEquals(name, result.getName(), "Returned author's name should equal the input name");
        assertNotNull(result.getId(), "Returned author's id should be non-null");
        assertFalse(result.isDeleted(), "Returned author's deleted flag should be false");
    }

    @Provide
    Arbitrary<String> validAuthorNames() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());
    }
}

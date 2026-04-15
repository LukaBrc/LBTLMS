package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 3: Create author round-trip
 *
 * For any valid author name, calling createAuthor(name) returns an Author entity
 * whose name equals the input name, whose id is non-null, and whose deleted flag is false.
 *
 * **Validates: Requirements 3.1**
 */
@Label("Feature: author-management, Property 3: Create author round-trip")
class CreateAuthorRoundTripPropertyTest {

    private final AtomicLong idCounter = new AtomicLong(1L);

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 3: Create author round-trip")
    @Label("createAuthor returns Author with matching name, non-null id, and deleted == false")
    void createAuthorRoundTrip(@ForAll("validAuthorNames") String name) {
        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);
        ValidationHandler validationHandler = new ValidationHandler();

        when(repo.save(any(Author.class))).thenAnswer(invocation -> {
            Author a = invocation.getArgument(0);
            a.setId(idCounter.getAndIncrement());
            return a;
        });

        AuthorService authorService = new AuthorService(repo, cache, validationHandler);

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

package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;

import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 6: Soft-delete sets flag
 *
 * For any existing non-deleted author, calling deleteAuthor(id) results in
 * the author's deleted field being true in the database, while preserving
 * all other fields.
 *
 * **Validates: Requirements 6.1**
 */
@Label("Feature: author-management, Property 6: Soft-delete sets flag")
class SoftDeleteSetsFlagPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 6: Soft-delete sets flag")
    @Label("deleteAuthor sets deleted to true while preserving name and id")
    void deleteAuthorSetsFlagAndPreservesFields(
            @ForAll("existingAuthorIds") Long authorId,
            @ForAll("validAuthorNames") String authorName) {

        // Set up mocks
        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);
        ValidationHandler validationHandler = new ValidationHandler();

        // Create the existing non-deleted author
        Author existingAuthor = Author.builder()
                .id(authorId)
                .name(authorName)
                .deleted(false)
                .build();

        // Mock findByIdAndDeletedFalse to return the existing author
        when(repo.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.of(existingAuthor));

        // Use ArgumentCaptor to capture the Author passed to save()
        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorService authorService = new AuthorService(repo, cache, validationHandler);

        // Act
        authorService.deleteAuthor(authorId);

        // Assert - verify the author saved to the repository
        Author savedAuthor = captor.getValue();
        assertTrue(savedAuthor.isDeleted(), "The author saved to the repository should have deleted == true");
        assertEquals(authorName, savedAuthor.getName(), "The author's name should be preserved (unchanged)");
        assertEquals(authorId, savedAuthor.getId(), "The author's id should be preserved (unchanged)");

        // Assert - verify cache evict was called with the correct id
        verify(cache).evict(authorId);
    }

    @Provide
    Arbitrary<Long> existingAuthorIds() {
        return Arbitraries.longs().between(1L, 10000L);
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

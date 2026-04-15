package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;
import com.lbt.services.ValidationHandler;

import net.jqwik.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 5: Update persists new values
 *
 * For any existing non-deleted author and any valid new name,
 * calling updateAuthor(id, newName) returns an author entity whose
 * name equals the new name and whose id is unchanged.
 *
 * **Validates: Requirements 5.1**
 */
@Label("Feature: author-management, Property 5: Update persists new values")
class UpdatePersistsNewValuesPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: author-management, Property 5: Update persists new values")
    @Label("updateAuthor returns author with new name and unchanged id")
    void updateAuthorPersistsNewValues(
            @ForAll("existingAuthorIds") Long originalId,
            @ForAll("validAuthorNames") String originalName,
            @ForAll("validAuthorNames") String newName) {

        // Set up mocks
        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);
        ValidationHandler validationHandler = new ValidationHandler();

        // Create the existing author
        Author existingAuthor = Author.builder()
                .id(originalId)
                .name(originalName)
                .deleted(false)
                .build();

        // Mock findByIdAndDeletedFalse to return the existing author
        when(repo.findByIdAndDeletedFalse(originalId)).thenReturn(Optional.of(existingAuthor));

        // Mock save to return its argument (simulating persistence)
        when(repo.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorService authorService = new AuthorService(repo, cache, validationHandler);

        // Act
        Author result = authorService.updateAuthor(originalId, newName);

        // Assert
        assertEquals(newName, result.getName(), "Returned author's name should equal the new name");
        assertEquals(originalId, result.getId(), "Returned author's id should be unchanged");
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

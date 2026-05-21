package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;

import net.jqwik.api.*;

import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SoftDeleteSetsFlagPropertyTest {

    @Property(tries = 100)
    @Tag("feature-author-management-property-6-soft-delete-sets-flag")
    void deleteAuthorSetsFlagAndPreservesFields(
            @ForAll("existingAuthorIds") Long authorId,
            @ForAll("validAuthorNames") String authorName) {

        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);

        Author existingAuthor = Author.builder()
                .id(authorId)
                .name(authorName)
                .deleted(false)
                .build();

        when(repo.findByIdAndDeletedFalse(authorId)).thenReturn(Optional.of(existingAuthor));

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorService authorService = new AuthorService(repo, cache);

        authorService.deleteAuthor(authorId);

        Author savedAuthor = captor.getValue();
        assertTrue(savedAuthor.isDeleted(), "The author saved to the repository should have deleted == true");
        assertEquals(authorName, savedAuthor.getName(), "The author's name should be preserved (unchanged)");
        assertEquals(authorId, savedAuthor.getId(), "The author's id should be preserved (unchanged)");

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

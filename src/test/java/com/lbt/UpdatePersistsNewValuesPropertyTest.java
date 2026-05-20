package com.lbt;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.AuthorCache;
import com.lbt.services.AuthorService;

import net.jqwik.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdatePersistsNewValuesPropertyTest {

    @Property(tries = 100)
    @Tag("feature-author-management-property-5-update-persists-new-values")
    void updateAuthorPersistsNewValues(
            @ForAll("existingAuthorIds") Long originalId,
            @ForAll("validAuthorNames") String originalName,
            @ForAll("validAuthorNames") String newName) {

        AuthorRepository repo = mock(AuthorRepository.class);
        AuthorCache cache = mock(AuthorCache.class);

        Author existingAuthor = Author.builder()
                .id(originalId)
                .name(originalName)
                .deleted(false)
                .build();

        when(repo.findByIdAndDeletedFalse(originalId)).thenReturn(Optional.of(existingAuthor));

        when(repo.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorService authorService = new AuthorService(repo, cache);

        Author result = authorService.updateAuthor(originalId, newName);

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

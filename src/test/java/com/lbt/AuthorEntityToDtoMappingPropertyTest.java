package com.lbt;

import com.lbt.dto.AuthorResponse;
import com.lbt.entities.Author;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 8: Author entity-to-DTO mapping
 *
 * For any Author entity, mapping it to an AuthorResponse DTO produces a DTO
 * whose id and name fields exactly match the entity's id and name.
 *
 * Validates: Requirements 8.2, 8.3
 */
@Label("Feature: author-management, Property 8: Author entity-to-DTO mapping")
class AuthorEntityToDtoMappingPropertyTest {

    /**
     * Helper that performs the same mapping the controller will use:
     * Author entity -> AuthorResponse DTO.
     */
    private AuthorResponse mapToResponse(Author author) {
        AuthorResponse r = new AuthorResponse();
        r.setId(author.getId());
        r.setName(author.getName());
        return r;
    }

    /**
     * For any Author entity with a random id and random non-blank name,
     * mapping to AuthorResponse produces a DTO whose id and name exactly
     * match the entity.
     *
     * Validates: Requirements 8.2, 8.3
     */
    @Property(tries = 100)
    @Tag("Feature: author-management, Property 8: Author entity-to-DTO mapping")
    @Label("Mapped AuthorResponse id and name match the Author entity")
    void mappedDtoMatchesEntity(
            @ForAll("randomAuthors") Author author
    ) {
        AuthorResponse dto = mapToResponse(author);

        assertEquals(author.getId(), dto.getId(),
                "AuthorResponse id must match Author entity id");
        assertEquals(author.getName(), dto.getName(),
                "AuthorResponse name must match Author entity name");
    }

    @Provide
    Arbitrary<Author> randomAuthors() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, Long.MAX_VALUE);
        Arbitrary<String> names = Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(150)
                .alpha()
                .filter(s -> !s.trim().isEmpty());

        return Combinators.combine(ids, names).as((id, name) ->
                Author.builder().id(id).name(name).deleted(false).build()
        );
    }
}

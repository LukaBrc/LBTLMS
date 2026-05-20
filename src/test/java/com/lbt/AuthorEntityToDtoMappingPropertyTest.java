package com.lbt;

import com.lbt.dto.AuthorResponse;
import com.lbt.entities.Author;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthorEntityToDtoMappingPropertyTest {

    private AuthorResponse mapToResponse(Author author) {
        AuthorResponse r = new AuthorResponse();
        r.setId(author.getId());
        r.setName(author.getName());
        return r;
    }

    @Property(tries = 100)
    @Tag("feature-author-management-property-8-author-entity-to-dto-mapping")
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

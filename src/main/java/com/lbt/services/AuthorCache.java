package com.lbt.services;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.services.cache.AbstractEntityCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorCache extends AbstractEntityCache<Author, Long> {

    private final AuthorRepository authorRepository;

    public AuthorCache(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    protected List<Author> loadAll() {
        return authorRepository.findByDeletedFalse();
    }

    @Override
    protected Long extractKey(Author author) {
        return author.getId();
    }

    @Override
    @Scheduled(fixedRateString = "${author.cache.refresh-interval-ms:300000}")
    public void refresh() {
        super.refresh();
    }
}

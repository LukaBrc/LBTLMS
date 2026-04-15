package com.lbt.services;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthorCache {

    private static final Logger logger = LoggerFactory.getLogger(AuthorCache.class);

    private final AuthorRepository authorRepository;
    private final ConcurrentHashMap<Long, Author> cache = new ConcurrentHashMap<>();

    public AuthorCache(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @PostConstruct
    public void init() {
        List<Author> authors = authorRepository.findByDeletedFalse();
        authors.forEach(author -> cache.put(author.getId(), author));
    }

    @Scheduled(fixedRateString = "${author.cache.refresh-interval-ms:300000}")
    public void refresh() {
        try {
            List<Author> authors = authorRepository.findByDeletedFalse();
            ConcurrentHashMap<Long, Author> newData = new ConcurrentHashMap<>();
            authors.forEach(author -> newData.put(author.getId(), author));
            cache.clear();
            cache.putAll(newData);
        } catch (Exception e) {
            logger.warn("Failed to refresh author cache, retaining stale data", e);
        }
    }

    public List<Author> getAll() {
        return new ArrayList<>(cache.values());
    }

    public Optional<Author> getById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public void put(Author author) {
        cache.put(author.getId(), author);
    }

    public void evict(Long id) {
        cache.remove(id);
    }
}

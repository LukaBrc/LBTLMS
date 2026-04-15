package com.lbt.services;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorCache authorCache;
    private final ValidationHandler validationHandler;

    public AuthorService(AuthorRepository authorRepository, AuthorCache authorCache, ValidationHandler validationHandler) {
        this.authorRepository = authorRepository;
        this.authorCache = authorCache;
        this.validationHandler = validationHandler;
    }

    public Author createAuthor(String name) {
        Author author = Author.builder().name(name).build();
        validationHandler.validate(author);
        Author saved = authorRepository.save(author);
        authorCache.put(saved);
        return saved;
    }

    public List<Author> getAllAuthors() {
        return authorCache.getAll();
    }

    public Author getAuthorById(Long id) {
        return authorCache.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + id));
    }

    public Author updateAuthor(Long id, String name) {
        Author author = authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + id));
        author.setName(name);
        validationHandler.validate(author);
        Author saved = authorRepository.save(author);
        authorCache.put(saved);
        return saved;
    }

    public void deleteAuthor(Long id) {
        Author author = authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + id));
        author.setDeleted(true);
        authorRepository.save(author);
        authorCache.evict(id);
    }
}

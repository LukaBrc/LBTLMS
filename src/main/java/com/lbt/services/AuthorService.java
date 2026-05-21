package com.lbt.services;

import com.lbt.entities.Author;
import com.lbt.repositories.AuthorRepository;
import com.lbt.validation.ValidationHandler;
import com.lbt.validation.ValidationHandlerResolver;
import com.lbt.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorCache authorCache;
    private final ValidationHandler validationHandler;

    public AuthorService(AuthorRepository authorRepository, AuthorCache authorCache) {
        this(authorRepository, authorCache, ValidationHandlerResolver.get());
    }

    @Autowired
    public AuthorService(AuthorRepository authorRepository, AuthorCache authorCache, ValidationHandler validationHandler) {
        this.authorRepository = authorRepository;
        this.authorCache = authorCache;
        this.validationHandler = validationHandler;
    }

    public Author createAuthor(String name) {
        Author author = Author.builder().name(name).build();
        validateAuthor(author);
        Author saved = authorRepository.save(author);
        authorCache.put(saved);
        return saved;
    }


    public List<Author> getAllAuthors() {
        return authorCache.getAll();
    }

    public List<Author> searchAuthorsByNameContains(String name) {
        List<Author> all = getAllAuthors();
        if (name == null || name.isBlank()) {
            return all;
        }
        String lowerFilter = name.toLowerCase();
        return all.stream()
                .filter(a -> a.getName().toLowerCase().contains(lowerFilter))
                .toList();
    }

    public Author getAuthorById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Author id must not be null.");
        }
        return authorCache.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + id));
    }

    public Author updateAuthor(Long id, String name) {
        Author author = authorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + id));
        author.setName(name);
        validateAuthor(author);
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

    private void validateAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("Author must not be null.");
        }
        List<ValidationError> errors = validationHandler.getValidationErrors(author);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0).message());
        }
    }
}

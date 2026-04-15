package com.lbt.controllers;

import com.lbt.dto.AuthorRequest;
import com.lbt.dto.AuthorResponse;
import com.lbt.entities.Author;
import com.lbt.services.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest request) {
        Author author = authorService.createAuthor(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(author));
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        List<AuthorResponse> response = authors.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        try {
            Author author = authorService.getAuthorById(id);
            return ResponseEntity.ok(toResponse(author));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(@PathVariable Long id,
                                                       @Valid @RequestBody AuthorRequest request) {
        try {
            Author author = authorService.updateAuthor(id, request.getName());
            return ResponseEntity.ok(toResponse(author));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private AuthorResponse toResponse(Author author) {
        AuthorResponse r = new AuthorResponse();
        r.setId(author.getId());
        r.setName(author.getName());
        return r;
    }
}

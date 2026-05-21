package com.lbt.controllers;

import com.lbt.dto.AuthorRequest;
import com.lbt.dto.AuthorResponse;
import com.lbt.dto.ApiMessageResponse;
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
    public ResponseEntity<List<AuthorResponse>> getAllAuthors(
            @RequestParam(required = false) String name) {
        List<Author> authors = (name == null || name.isBlank())
                ? authorService.getAllAuthors()
                : authorService.searchAuthorsByNameContains(name);
        List<AuthorResponse> response = authors.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        return ResponseEntity.ok(toResponse(author));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(@PathVariable Long id,
                                                       @Valid @RequestBody AuthorRequest request) {
        Author author = authorService.updateAuthor(id, request.getName());
        return ResponseEntity.ok(toResponse(author));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.ok(ApiMessageResponse.builder()
                .message("Author deleted successfully")
                .build());
    }


    private AuthorResponse toResponse(Author author) {
        AuthorResponse r = new AuthorResponse();
        r.setId(author.getId());
        r.setName(author.getName());
        return r;
    }
}

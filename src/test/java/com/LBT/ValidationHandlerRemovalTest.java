package com.lbt;

import com.lbt.controllers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the ValidationHandler class has been fully removed from the codebase
 * and that GlobalExceptionHandler continues to handle exceptions correctly.
 *
 * Requirements: 6.4, 7.5, 7.6
 */
class ValidationHandlerRemovalTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ========== Static Analysis: No ValidationHandler references ==========

    @Test
    @DisplayName("No Java source file imports ValidationHandler")
    void noValidationHandlerImportsExist() throws IOException {
        Path srcMain = Paths.get("src", "main", "java");

        // Ensure the source directory exists before scanning
        assertTrue(Files.exists(srcMain), "src/main/java directory must exist");

        try (Stream<Path> javaFiles = Files.walk(srcMain)
                .filter(p -> p.toString().endsWith(".java"))) {

            List<String> violations = javaFiles
                    .flatMap(path -> {
                        try {
                            return Files.readAllLines(path).stream()
                                    .filter(line -> line.contains("import") && line.contains("ValidationHandler"))
                                    .map(line -> path + ": " + line.trim());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read file: " + path, e);
                        }
                    })
                    .collect(Collectors.toList());

            assertTrue(violations.isEmpty(),
                    "Found ValidationHandler imports in source files:\n" +
                            String.join("\n", violations));
        }
    }

    // ========== GlobalExceptionHandler tests ==========

    @Test
    @DisplayName("handleIllegalArgument returns HTTP 400 with exception message")
    void handleIllegalArgument_returnsHttp400WithMessage() {
        // Validates: Requirement 7.5
        IllegalArgumentException ex = new IllegalArgumentException("Book title must not be empty.");

        ResponseEntity<String> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Book title must not be empty.", response.getBody());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation returns HTTP 409 with fixed message")
    void handleDataIntegrityViolation_returnsHttp409WithFixedMessage() {
        // Validates: Requirement 7.6
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Duplicate entry for key 'isbn'");

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("A database constraint was violated", response.getBody());
    }

    @Test
    @DisplayName("handleValidationErrors returns HTTP 400 with field-error map")
    void handleValidationErrors_returnsHttp400WithFieldErrorMap() {
        // Validates: Requirement 7.6
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "title", "Title is required"));
        bindingResult.addError(new FieldError("target", "isbn", "ISBN is required"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Title is required", response.getBody().get("title"));
        assertEquals("ISBN is required", response.getBody().get("isbn"));
        assertEquals(2, response.getBody().size());
    }
}

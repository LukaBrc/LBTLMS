package com.lbt;

import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.validation.ValidationHandler;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationHandlerRemovalTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }


    @Test
    @DisplayName("ValidationHandler class exists in the validation package")
    void validationHandlerClassExists() {
        assertNotNull(ValidationHandler.class);
    }

    @Test
    @DisplayName("Entity classes do not declare getValidationErrors directly")
    void entitiesDoNotDeclareGetValidationErrors() {
        List<Class<?>> entityClasses = List.of(Author.class, Book.class, Member.class, BorrowTransaction.class);
        for (Class<?> entityClass : entityClasses) {
            boolean declaresMethod = Arrays.stream(entityClass.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("getValidationErrors") && m.getParameterCount() == 0);
            assertFalse(declaresMethod,
                    () -> entityClass.getSimpleName() + " should not declare getValidationErrors directly.");
        }
    }

    @Test
    @DisplayName("ValidationHandler imports exist in service layer")
    void validationHandlerImportsExistInServices() throws IOException {
        Path srcMain = Paths.get("src", "main", "java");

        assertTrue(Files.exists(srcMain), "src/main/java directory must exist");

        try (Stream<Path> javaFiles = Files.walk(srcMain)
                .filter(p -> p.toString().endsWith(".java"))) {

            List<String> imports = javaFiles
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

            assertFalse(imports.isEmpty(), "Expected ValidationHandler to be imported by service classes.");
        }
    }


    @Test
    @DisplayName("handleIllegalArgument returns HTTP 400 with exception message")
    void handleIllegalArgument_returnsHttp400WithMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Book title must not be empty.");

        ResponseEntity<String> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Book title must not be empty.", response.getBody());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation returns HTTP 409 with fixed message")
    void handleDataIntegrityViolation_returnsHttp409WithFixedMessage() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Duplicate entry for key 'isbn'");

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("A database constraint was violated", response.getBody());
    }

    @Test
    @DisplayName("handleValidationErrors returns HTTP 400 with field-error map")
    void handleValidationErrors_returnsHttp400WithFieldErrorMap() {
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

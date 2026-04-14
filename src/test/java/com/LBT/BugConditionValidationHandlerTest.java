package com.lbt;

import com.lbt.services.BookService;
import com.lbt.services.ValidationHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test — ValidationHandler Spring bean injection (Test 4).
 *
 * Uses @SpringBootTest to load the full Spring context.
 * On UNFIXED code, context fails to load because ValidationHandler is missing @Service.
 *
 * Validates: Requirements 1.5
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Bug Condition Exploration — ValidationHandler Bean Injection")
class BugConditionValidationHandlerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Test 4 — Bug 5: ValidationHandler should be available as a Spring bean")
    void validationHandlerSpringBeanInjection() {
        // Validates: Requirements 1.5
        // Bug 5: ValidationHandler is missing @Service/@Component annotation
        // On unfixed code, Spring context fails to load with NoSuchBeanDefinitionException

        assertDoesNotThrow(() -> {
            ValidationHandler handler = applicationContext.getBean(ValidationHandler.class);
            assertNotNull(handler, "ValidationHandler should be a Spring-managed bean");
        }, "ValidationHandler should be injectable — currently missing @Service annotation");

        // Also verify BookService can be injected (it depends on ValidationHandler)
        assertDoesNotThrow(() -> {
            BookService bookService = applicationContext.getBean(BookService.class);
            assertNotNull(bookService, "BookService should be injectable with ValidationHandler");
        }, "BookService should be injectable — depends on ValidationHandler bean");
    }
}

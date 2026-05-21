package com.lbt.validation;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Exposes a shared ValidationHandler.
 * In Spring runtime this points to the managed bean; plain unit tests use a local fallback.
 */
@Component
public class ValidationHandlerResolver {

    private static final ValidationHandler FALLBACK_HANDLER = new ValidationHandler();
    private static volatile ValidationHandler springManagedHandler;
    private final ValidationHandler instanceHandler;

    public ValidationHandlerResolver(ValidationHandler validationHandler) {
        this.instanceHandler = validationHandler;
        springManagedHandler = validationHandler;
    }

    @PreDestroy
    void clear() {
        if (springManagedHandler == instanceHandler) {
            springManagedHandler = null;
        }
    }

    public static ValidationHandler get() {
        return springManagedHandler != null ? springManagedHandler : FALLBACK_HANDLER;
    }
}


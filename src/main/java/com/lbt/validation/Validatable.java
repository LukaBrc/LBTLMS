package com.lbt.validation;

import java.util.List;

public interface Validatable {
    default boolean isValid() {
        return ValidationHandlerResolver.get().isValid(this);
    }

    default List<ValidationError> getValidationErrors() {
        return ValidationHandlerResolver.get().getValidationErrors(this);
    }
}

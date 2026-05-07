package com.lbt.validation;

import java.util.List;

public interface Validatable {

    default boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    List<ValidationError> getValidationErrors();
}

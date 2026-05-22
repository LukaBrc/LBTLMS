package com.lbt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Author id is required")
    private Long authorId;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Genre is required")
    private String genre;

    @Min(value = 1, message = "Total copies must be at least 1")
    private int totalCopies;
}
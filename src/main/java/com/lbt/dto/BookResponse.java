package com.lbt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String isbn;
    private String title;
    private Long authorId;
    private String authorName;
    private String genre;
    private int totalCopies;
    private int availableCopies;
}
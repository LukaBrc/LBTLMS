package com.lbt.dto;

import lombok.Data;

@Data
public class BookResponse {
    private String isbn;
    private String title;
    private Long authorId;
    private String authorName;
    private String genre;
    private int totalCopies;
    private int availableCopies;
}
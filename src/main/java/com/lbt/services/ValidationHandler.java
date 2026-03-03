package com.lbt.services;

import com.lbt.entities.Book;

public class ValidationHandler {

	public void validate(Book book) {
		if (book == null) {
	        throw new IllegalArgumentException("Book must not be null.");
        }
		if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title must not be empty.");
		}
		if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Book author must not be empty.");
		}
		if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN must not be empty.");
        }
		
	}
	
}

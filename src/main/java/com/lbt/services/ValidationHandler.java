package com.lbt.services;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import org.springframework.stereotype.Service;

@Service
public class ValidationHandler {

	public void validate(Book book) {
		if (book == null) {
	        throw new IllegalArgumentException("Book must not be null.");
        }
		if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title must not be empty.");
		}
		if (book.getAuthor() == null) {
            throw new IllegalArgumentException("Book author must not be null.");
		}
		if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN must not be empty.");
        }
		
	}

	public void validate(Author author) {
		if (author == null) {
			throw new IllegalArgumentException("Author must not be null.");
		}
		if (author.getName() == null || author.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Author name must not be empty.");
		}
		if (author.getName().length() > 150) {
			throw new IllegalArgumentException("Author name must not exceed 150 characters.");
		}
	}
	
}

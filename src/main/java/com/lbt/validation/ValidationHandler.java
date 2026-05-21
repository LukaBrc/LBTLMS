package com.lbt.validation;

import com.lbt.entities.Author;
import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValidationHandler {

    public boolean isValid(Object entity) {
        return getValidationErrors(entity).isEmpty();
    }

    public List<ValidationError> getValidationErrors(Object entity) {
        if (entity instanceof Author author) {
            return validateAuthor(author);
        }
        if (entity instanceof Book book) {
            return validateBook(book);
        }
        if (entity instanceof Member member) {
            return validateMember(member);
        }
        if (entity instanceof BorrowTransaction borrowTransaction) {
            return validateBorrowTransaction(borrowTransaction);
        }
        throw new IllegalArgumentException("No validation rules configured for type: "
                + (entity == null ? "null" : entity.getClass().getName()));
    }

    private List<ValidationError> validateAuthor(Author author) {
        List<ValidationError> errors = new ArrayList<>();
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            errors.add(new ValidationError("name", "Author name must not be empty."));
        } else if (author.getName().length() > 150) {
            errors.add(new ValidationError("name", "Author name must not exceed 150 characters."));
        }
        return errors;
    }

    private List<ValidationError> validateBook(Book book) {
        List<ValidationError> errors = new ArrayList<>();
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            errors.add(new ValidationError("title", "Book title must not be empty."));
        }
        if (book.getAuthor() == null) {
            errors.add(new ValidationError("author", "Book author must not be null."));
        }
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            errors.add(new ValidationError("isbn", "Book ISBN must not be empty."));
        }
        return errors;
    }

    private List<ValidationError> validateMember(Member member) {
        List<ValidationError> errors = new ArrayList<>();
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            errors.add(new ValidationError("name", "Member name must not be empty."));
        } else if (member.getName().length() > 150) {
            errors.add(new ValidationError("name", "Member name must not exceed 150 characters."));
        }
        if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
            errors.add(new ValidationError("memberId", "Member ID must not be empty."));
        } else if (member.getMemberId().length() > 50) {
            errors.add(new ValidationError("memberId", "Member ID must not exceed 50 characters."));
        }
        if (member.getContact() == null || member.getContact().trim().isEmpty()) {
            errors.add(new ValidationError("contact", "Member contact must not be empty."));
        } else if (member.getContact().length() > 200) {
            errors.add(new ValidationError("contact", "Member contact must not exceed 200 characters."));
        }
        return errors;
    }

    private List<ValidationError> validateBorrowTransaction(BorrowTransaction borrowTransaction) {
        List<ValidationError> errors = new ArrayList<>();
        if (borrowTransaction.getBookIsbn() == null || borrowTransaction.getBookIsbn().trim().isEmpty()) {
            errors.add(new ValidationError("bookIsbn", "Book ISBN must not be empty."));
        } else if (borrowTransaction.getBookIsbn().length() > 50) {
            errors.add(new ValidationError("bookIsbn", "Book ISBN must not exceed 50 characters."));
        }
        if (borrowTransaction.getMemberId() == null || borrowTransaction.getMemberId().trim().isEmpty()) {
            errors.add(new ValidationError("memberId", "Member ID must not be empty."));
        } else if (borrowTransaction.getMemberId().length() > 50) {
            errors.add(new ValidationError("memberId", "Member ID must not exceed 50 characters."));
        }
        if (borrowTransaction.getBorrowDate() == null) {
            errors.add(new ValidationError("borrowDate", "Borrow date must not be null."));
        }
        return errors;
    }
}


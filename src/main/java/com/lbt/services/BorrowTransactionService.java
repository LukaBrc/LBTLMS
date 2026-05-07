package com.lbt.services;

import org.springframework.stereotype.Service;

import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.validation.Validatable;
import com.lbt.validation.ValidationError;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowTransactionService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;

    public BorrowTransactionService(BookRepository bookRepository,
                                    MemberRepository memberRepository,
                                    BorrowTransactionRepository transactionRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    public boolean borrowBook(String isbn, String memberId) {
        Book book = bookRepository.findByIsbn(isbn);
        Member member = memberRepository.findByMemberId(memberId);

        if (book == null) return false;
        if (member == null) return false;
        if (!member.canBorrow()) return false;
        if (!book.borrowCopy()) return false;

        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn(isbn);
        tx.setMemberId(memberId);
        tx.setBorrowDate(LocalDate.now());

        validateEntity(tx, "BorrowTransaction");

        transactionRepository.save(tx);
        member.borrowBook(isbn);
        bookRepository.save(book);

        return true;
    }

    public boolean returnBook(String isbn, String memberId) {
        Book book = bookRepository.findByIsbn(isbn);
        Member member = memberRepository.findByMemberId(memberId);

        if (book == null || member == null) return false;

        var activeTx = transactionRepository.findByBookIsbnAndMemberIdAndReturnDateIsNull(isbn, memberId);
        if (activeTx.isEmpty()) return false;

        book.returnCopy();
        member.returnBook(isbn);

        BorrowTransaction tx = activeTx.get();
        tx.setReturnDate(LocalDate.now());
        transactionRepository.save(tx);
        bookRepository.save(book);

        return true;
    }

    public List<BorrowTransaction> getOverdueBooks() {
        return transactionRepository.findOverdue(LocalDate.now());
    }

    public List<BorrowTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    private void validateEntity(Validatable entity, String entityName) {
        if (entity == null) {
            throw new IllegalArgumentException(entityName + " must not be null.");
        }
        if (!entity.isValid()) {
            String message = entity.getValidationErrors().stream()
                .map(ValidationError::message)
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
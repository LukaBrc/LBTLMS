package com.lbt.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lbt.entities.Book;
import com.lbt.entities.BorrowTransaction;
import com.lbt.entities.Member;
import com.lbt.repositories.BookRepository;
import com.lbt.repositories.BorrowTransactionRepository;
import com.lbt.repositories.MemberRepository;
import com.lbt.validation.ValidationHandler;
import com.lbt.validation.ValidationHandlerResolver;
import com.lbt.validation.ValidationError;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowTransactionService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final ValidationHandler validationHandler;
    private final BookCache bookCache;

    public BorrowTransactionService(BookRepository bookRepository,
                                    MemberRepository memberRepository,
                                    BorrowTransactionRepository transactionRepository) {
        this(bookRepository, memberRepository, transactionRepository, ValidationHandlerResolver.get(), null);
    }

    @Autowired
    public BorrowTransactionService(BookRepository bookRepository,
                                    MemberRepository memberRepository,
                                    BorrowTransactionRepository transactionRepository,
                                    ValidationHandler validationHandler,
                                    BookCache bookCache) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        this.validationHandler = validationHandler;
        this.bookCache = bookCache;
    }

    @Transactional
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
        Book savedBook = bookRepository.save(book);
        if (savedBook != null && bookCache != null) {
            bookCache.put(savedBook);
        }

        return true;
    }

    @Transactional
    public boolean returnBook(String isbn, String memberId) {
        Book book = bookRepository.findByIsbn(isbn);
        Member member = memberRepository.findByMemberId(memberId);

        if (book == null || member == null) return false;

        List<BorrowTransaction> activeTxs = transactionRepository
                .findByBookIsbnAndMemberIdAndReturnDateIsNullOrderByBorrowDateAscIdAsc(isbn, memberId);
        if (activeTxs.isEmpty()) return false;

        book.returnCopy();
        member.returnBook(isbn);

        BorrowTransaction tx = activeTxs.get(0);
        tx.setReturnDate(LocalDate.now());
        transactionRepository.save(tx);
        Book savedBook = bookRepository.save(book);
        if (savedBook != null && bookCache != null) {
            bookCache.put(savedBook);
        }

        return true;
    }

    public List<BorrowTransaction> getOverdueBooks() {
        return transactionRepository.findOverdue(LocalDate.now());
    }

    public List<BorrowTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    private void validateEntity(Object entity, String entityName) {
        if (entity == null) {
            throw new IllegalArgumentException(entityName + " must not be null.");
        }
        List<ValidationError> errors = validationHandler.getValidationErrors(entity);
        if (!errors.isEmpty()) {
            String message = errors.stream()
                .map(ValidationError::message)
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
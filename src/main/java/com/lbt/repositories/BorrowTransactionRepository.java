package com.lbt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lbt.entities.BorrowTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    // Exact matches to your old IBorrowTransactionRepository
    List<BorrowTransaction> findByReturnDateIsNull();

    List<BorrowTransaction> findByMemberId(String memberId);

    List<BorrowTransaction> findByBookIsbn(String isbn);

    List<BorrowTransaction> findByMemberIdAndReturnDateIsNull(String memberId);

    Optional<BorrowTransaction> findByBookIsbnAndMemberIdAndReturnDateIsNull(
            String bookIsbn, String memberId);

    @Query("SELECT t FROM BorrowTransaction t " +
           "WHERE t.returnDate IS NULL AND t.dueDate < :checkDate")
    List<BorrowTransaction> findOverdue(@Param("checkDate") LocalDate checkDate);
    
    @Query("SELECT t.bookIsbn FROM BorrowTransaction t " +
            "WHERE t.memberId = :memberId AND t.returnDate IS NULL")
     List<String> findActiveBookIsbnsByMemberId(@Param("memberId") String memberId);
}
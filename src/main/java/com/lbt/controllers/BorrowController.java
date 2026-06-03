package com.lbt.controllers;

import com.lbt.dto.ApiMessageResponse;
import com.lbt.dto.BorrowRequest;
import com.lbt.dto.BorrowTransactionResponse;
import com.lbt.entities.BorrowTransaction;
import com.lbt.services.BorrowTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
@CrossOrigin(origins = "*")
public class BorrowController {

    private final BorrowTransactionService borrowService;

    public BorrowController(BorrowTransactionService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ResponseEntity<ApiMessageResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        boolean success = borrowService.borrowBook(request.getIsbn(), request.getMemberId());
        if (!success) {
            throw new IllegalArgumentException("Cannot borrow book (not available, limit reached, etc.)");
        }
        return ResponseEntity.ok(message("Book borrowed successfully"));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiMessageResponse> returnBook(@Valid @RequestBody BorrowRequest request) {
        boolean success = borrowService.returnBook(request.getIsbn(), request.getMemberId());
        if (!success) {
            throw new IllegalArgumentException("No active borrow found");
        }
        return ResponseEntity.ok(message("Book returned successfully"));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowTransactionResponse>> getOverdue() {
        List<BorrowTransactionResponse> response = borrowService.getOverdueBooks().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BorrowTransactionResponse>> getActiveLoans(@RequestParam(required = false) String memberName) {
        List<BorrowTransactionResponse> response = borrowService.getActiveLoans(memberName).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }


    private ApiMessageResponse message(String message) {
        return ApiMessageResponse.builder()
                .message(message)
                .build();
    }

    private BorrowTransactionResponse toResponse(BorrowTransaction tx) {
        return BorrowTransactionResponse.builder()
                .id(tx.getId())
                .bookIsbn(tx.getBookIsbn())
                .memberId(tx.getMemberId())
                .borrowDate(tx.getBorrowDate())
                .dueDate(tx.getDueDate())
                .returnDate(tx.getReturnDate())
                .active(tx.isActive())
                .build();
    }
}
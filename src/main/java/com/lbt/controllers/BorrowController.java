package com.lbt.controllers;

import com.lbt.dto.ApiErrorResponse;
import com.lbt.dto.ApiMessageResponse;
import com.lbt.dto.BorrowRequest;
import com.lbt.entities.BorrowTransaction;
import com.lbt.services.BorrowTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<?> borrowBook(@RequestBody BorrowRequest request) {
        boolean success = borrowService.borrowBook(request.getIsbn(), request.getMemberId());
        return success 
                ? ResponseEntity.ok(message("Book borrowed successfully"))
                : ResponseEntity.badRequest().body(error("Cannot borrow book (not available, limit reached, etc.)"));
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody BorrowRequest request) {
        boolean success = borrowService.returnBook(request.getIsbn(), request.getMemberId());
        return success 
                ? ResponseEntity.ok(message("Book returned successfully"))
                : ResponseEntity.badRequest().body(error("No active borrow found"));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowTransaction>> getOverdue() {
        return ResponseEntity.ok(borrowService.getOverdueBooks());
    }

    private ApiErrorResponse error(String message) {
        return ApiErrorResponse.builder()
                .message(message)
                .fieldErrors(Collections.emptyMap())
                .build();
    }

    private ApiMessageResponse message(String message) {
        return ApiMessageResponse.builder()
                .message(message)
                .build();
    }
}
package com.lbt.controllers;

import com.lbt.dto.BorrowRequest;
import com.lbt.entities.BorrowTransaction;
import com.lbt.services.BorrowTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@CrossOrigin(origins = "*")
public class BorrowController {

    private final BorrowTransactionService borrowService;

    public BorrowController(BorrowTransactionService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ResponseEntity<String> borrowBook(@RequestBody BorrowRequest request) {
        boolean success = borrowService.borrowBook(request.getIsbn(), request.getMemberId());
        return success 
                ? ResponseEntity.ok("Book borrowed successfully")
                : ResponseEntity.badRequest().body("Cannot borrow book (not available, limit reached, etc.)");
    }

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestBody BorrowRequest request) {
        boolean success = borrowService.returnBook(request.getIsbn(), request.getMemberId());
        return success 
                ? ResponseEntity.ok("Book returned successfully")
                : ResponseEntity.badRequest().body("No active borrow found");
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowTransaction>> getOverdue() {
        return ResponseEntity.ok(borrowService.getOverdueBooks());
    }
}
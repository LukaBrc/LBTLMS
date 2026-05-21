package com.lbt;

import com.lbt.controllers.BorrowController;
import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.entities.BorrowTransaction;
import com.lbt.services.BorrowTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({BorrowController.class, GlobalExceptionHandler.class})
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowTransactionService borrowService;

    @Test
    void borrowBook_returns200OnSuccess() throws Exception {
        when(borrowService.borrowBook("ISBN-1", "M001")).thenReturn(true);

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"isbn":"ISBN-1","memberId":"M001"}
                            """))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully"));
    }

    @Test
    void borrowBook_returns400OnFailure() throws Exception {
        when(borrowService.borrowBook("ISBN-1", "M001")).thenReturn(false);

        mockMvc.perform(post("/api/v1/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"isbn":"ISBN-1","memberId":"M001"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnBook_returns200OnSuccess() throws Exception {
        when(borrowService.returnBook("ISBN-1", "M001")).thenReturn(true);

        mockMvc.perform(post("/api/v1/borrows/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"isbn":"ISBN-1","memberId":"M001"}
                            """))
                .andExpect(status().isOk())
                .andExpect(content().string("Book returned successfully"));
    }

    @Test
    void returnBook_returns400OnFailure() throws Exception {
        when(borrowService.returnBook("ISBN-1", "M001")).thenReturn(false);

        mockMvc.perform(post("/api/v1/borrows/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"isbn":"ISBN-1","memberId":"M001"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOverdue_returns200WithList() throws Exception {
        BorrowTransaction tx = new BorrowTransaction();
        tx.setBookIsbn("ISBN-1");
        tx.setMemberId("M001");
        tx.setBorrowDate(LocalDate.now().minusDays(30));
        when(borrowService.getOverdueBooks()).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/v1/borrows/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookIsbn").value("ISBN-1"));
    }
}

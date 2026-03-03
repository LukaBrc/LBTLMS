package com.lbt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotBlank
    private String isbn;

    @NotBlank
    private String memberId;
}
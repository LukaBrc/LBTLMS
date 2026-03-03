package com.lbt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Member ID is required")
    private String memberId;

    @NotBlank(message = "Contact is required")
    private String contact;
}
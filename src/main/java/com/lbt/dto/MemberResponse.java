package com.lbt.dto;

import lombok.Data;

import java.util.List;

@Data
public class MemberResponse {
    private String memberId;
    private String name;
    private String contact;
    private List<String> borrowedIsbns;
}
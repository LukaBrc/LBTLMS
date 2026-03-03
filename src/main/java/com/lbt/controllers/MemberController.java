package com.lbt.controllers;

import com.lbt.dto.MemberRequest;
import com.lbt.dto.MemberResponse;
import com.lbt.entities.Member;
import com.lbt.services.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> registerMember(@Valid @RequestBody MemberRequest request) {
        memberService.registerMember(request.getName(), request.getMemberId(), request.getContact());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        List<MemberResponse> response = members.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable String memberId) {
        Member member = memberService.findById(memberId);
        return member != null 
                ? ResponseEntity.ok(toResponse(member)) 
                : ResponseEntity.notFound().build();
    }

    private MemberResponse toResponse(Member member) {
        MemberResponse r = new MemberResponse();
        r.setMemberId(member.getMemberId());
        r.setName(member.getName());
        r.setContact(member.getContact());
        r.setBorrowedIsbns(member.getBorrowedIsbns());
        return r;
    }
}
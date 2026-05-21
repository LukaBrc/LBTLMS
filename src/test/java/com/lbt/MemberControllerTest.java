package com.lbt;

import com.lbt.controllers.GlobalExceptionHandler;
import com.lbt.controllers.MemberController;
import com.lbt.entities.Member;
import com.lbt.services.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MemberController.class, GlobalExceptionHandler.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    private Member createSampleMember() {
        Member m = new Member();
        m.setMemberId("M001");
        m.setName("Alice");
        m.setContact("alice@test.com");
        m.setBorrowedIsbns(new ArrayList<>());
        return m;
    }

    @Test
    void postMember_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Alice","memberId":"M001","contact":"alice@test.com"}
                            """))
                .andExpect(status().isCreated());
        verify(memberService).registerMember("Alice", "M001", "alice@test.com");
    }

    @Test
    void postMember_returns400WhenNameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"memberId":"M001","contact":"alice@test.com"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMembers_returns200() throws Exception {
        Member m = createSampleMember();
        when(memberService.getAllMembers()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value("M001"));
    }

    @Test
    void getMember_returns200() throws Exception {
        Member m = createSampleMember();
        when(memberService.findById("M001")).thenReturn(m);

        mockMvc.perform(get("/api/v1/members/M001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getMember_returns404WhenNotFound() throws Exception {
        when(memberService.findById("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/v1/members/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putMember_returns200() throws Exception {
        Member updated = createSampleMember();
        updated.setName("Bob");
        updated.setContact("bob@test.com");
        when(memberService.updateMember("M001", "Bob", "bob@test.com")).thenReturn(updated);

        mockMvc.perform(put("/api/v1/members/M001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Bob","memberId":"M001","contact":"bob@test.com"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void putMember_returns400WhenNotFound() throws Exception {
        when(memberService.updateMember(eq("UNKNOWN"), any(), any()))
                .thenThrow(new IllegalArgumentException("Member not found: UNKNOWN"));

        mockMvc.perform(put("/api/v1/members/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Bob","memberId":"UNKNOWN","contact":"bob@test.com"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMember_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/members/M001"))
                .andExpect(status().isNoContent());
        verify(memberService).deleteMember("M001");
    }

    @Test
    void deleteMember_returns400WhenNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Member not found: UNKNOWN"))
                .when(memberService).deleteMember("UNKNOWN");

        mockMvc.perform(delete("/api/v1/members/UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}

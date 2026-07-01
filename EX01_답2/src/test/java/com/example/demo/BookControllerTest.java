package com.example.demo;

import com.example.demo.Controller.BookController;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Service.LibraryService;
import com.example.demo.Exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    private ResultActions postLend(String json) throws Exception {
        return mockMvc.perform(post("/api/library/lend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    @DisplayName("대출 정상: 200 과 lendId 를 반환한다")
    void lend_정상_200() throws Exception {
        Lend stub = new Lend();
        stub.setId(10L);
        stub.setDueAt(LocalDateTime.now().plusDays(14));
        when(libraryService.lend(eq(1L), anyString())).thenReturn(stub);

        postLend("{\"bookId\":1,\"member\":\"kim\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lendId").value(10));
    }

    @Test
    @DisplayName("대출 검증 실패: bookId 누락/member 공백이면 400")
    void lend_검증실패_400() throws Exception {
        postLend("{\"bookId\":null,\"member\":\"\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대출 비즈니스 예외: 재고 없음이면 400 + error 메시지")
    void lend_재고없음_400() throws Exception {
        when(libraryService.lend(eq(2L), anyString()))
                .thenThrow(new BizException("대출 가능한 재고가 없습니다."));

        postLend("{\"bookId\":2,\"member\":\"kim\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}

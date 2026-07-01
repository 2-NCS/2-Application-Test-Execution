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

@WebMvcTest(BookController.class) // BookController 웹 계층만 로드하는 슬라이스 테스트
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc; // 컨트롤러 요청/응답을 시뮬레이션하는 객체

    @MockitoBean
    private LibraryService libraryService; // LibraryService를 가짜(Mock)로 대체

    private ResultActions postLend(String json) throws Exception {
        // 테스트 클래스 내부에서만 사용하는 private 헬퍼 메서드
        // 반복되는 MockMvc POST 요청 코드를 재사용하기 위해 분리
        // 반환 타입 ResultActions: MockMvc 수행 결과에 대해 andExpect(), andDo() 등을 체이닝할 수 있게 해줌

        return mockMvc.perform(post("/api/library/lend")
                // MockMvc를 통해 실제 서버 기동 없이 "/api/library/lend" 경로로 HTTP POST 요청을 시뮬레이션
                // (스프링 시큐리티, 컨트롤러, 필터 등을 포함한 요청/응답 흐름을 애플리케이션 컨텍스트 내에서 테스트)

                .contentType(MediaType.APPLICATION_JSON)
                // 요청 헤더의 Content-Type을 "application/json"으로 설정
                // 컨트롤러가 @RequestBody로 JSON을 파싱할 수 있도록 명시

                .content(json));
        // 요청 바디에 파라미터로 전달받은 JSON 문자열을 그대로 담아 전송
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

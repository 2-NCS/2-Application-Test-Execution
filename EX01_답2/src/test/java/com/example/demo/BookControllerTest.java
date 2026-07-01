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
        // 테스트 목적을 설명하는 표시용 이름 (테스트 리포트에 표시됨)
    void lend_정상_200() throws Exception { // 테스트 메서드, MockMvc 등에서 체크된 예외(IOException 등)를 던질 수 있어 throws Exception 선언
        Lend stub = new Lend(); // 서비스 계층이 반환할 가짜(스텁) 결과 객체 생성
        stub.setId(10L); // 스텁 객체의 대출 ID를 10으로 설정 (검증할 때 사용할 값)
        stub.setDueAt(LocalDateTime.now().plusDays(14)); // 반납 예정일을 현재로부터 14일 뒤로 설정

        when(libraryService.lend(eq(1L), anyString())).thenReturn(stub);
        // libraryService.lend() 메서드가 첫 번째 인자로 정확히 1L, 두 번째 인자로 임의의 문자열이 들어올 때
        // 위에서 만든 stub 객체를 반환하도록 목(mock) 객체의 동작을 정의

        postLend("{\"bookId\":1,\"member\":\"kim\"}")
                // 대출 요청 API를 호출하는 커스텀 헬퍼 메서드 (내부적으로 MockMvc의 POST 요청을 수행할 것으로 추정)
                // JSON 바디로 bookId=1, member="kim" 을 전달

                .andExpect(status().isOk())
                // HTTP 응답 상태 코드가 200(OK)인지 검증

                .andExpect(jsonPath("$.lendId").value(10));
        // 응답 JSON 바디의 "lendId" 필드 값이 10인지 검증 (stub.setId(10L)과 매칭되는지 확인)
    }

    @Test
    @DisplayName("대출 검증 실패: bookId 누락/member 공백이면 400")
        // 테스트 리포트에 표시될 이름: bookId가 없거나(null) member가 빈 문자열일 때 400을 반환해야 함을 설명
    void lend_검증실패_400() throws Exception {
        // 테스트 메서드, MockMvc 수행 중 발생할 수 있는 체크 예외를 던지기 위해 throws Exception 선언

        postLend("{\"bookId\":null,\"member\":\"\"}")
                // 대출 요청 API 호출. bookId를 null로, member를 빈 문자열("")로 채운 잘못된 요청 바디 전송
                // (Bean Validation 등의 @NotNull, @NotBlank 검증에 걸리도록 의도된 입력값)

                .andExpect(status().isBadRequest());
        // HTTP 응답 상태 코드가 400(Bad Request)인지 검증
        // 컨트롤러/DTO에 설정된 검증 어노테이션이 정상 동작하여
        // 잘못된 입력을 걸러내는지 확인하는 테스트
    }

    @Test
    @DisplayName("대출 비즈니스 예외: 재고 없음이면 400 + error 메시지")
        // 테스트 리포트 표시 이름: 재고가 없는 상황(비즈니스 예외)에서 400과 error 메시지를 반환해야 함을 설명
    void lend_재고없음_400() throws Exception {
        // 테스트 메서드, MockMvc 수행 중 발생할 수 있는 체크 예외를 던지기 위해 throws Exception 선언

        when(libraryService.lend(eq(2L), anyString()))
                .thenThrow(new BizException("대출 가능한 재고가 없습니다."));
        // libraryService.lend()가 첫 번째 인자로 정확히 2L, 두 번째 인자로 임의의 문자열이 들어올 때
        // 정상 반환 대신 BizException(비즈니스 예외)을 던지도록 목(mock) 객체의 동작을 정의
        // (재고 부족 등 도메인 규칙 위반 상황을 시뮬레이션)

        postLend("{\"bookId\":2,\"member\":\"kim\"}")
                // 대출 요청 API 호출. bookId=2, member="kim"으로 요청 바디 전송
                // (앞서 스터빙한 조건과 일치시켜 BizException이 발생하도록 유도)

                .andExpect(status().isBadRequest())
                // HTTP 응답 상태 코드가 400(Bad Request)인지 검증
                // 서비스에서 던진 BizException을 컨트롤러/전역 예외 핸들러(@ExceptionHandler 등)가
                // 잡아서 400으로 변환하는지 확인

                .andExpect(jsonPath("$.error").exists());
        // 응답 JSON 바디에 "error" 필드가 존재하는지 검증
        // 예외 메시지("대출 가능한 재고가 없습니다.")가 error 필드 등을 통해
        // 클라이언트에게 전달되는지 확인 (단, 값 자체는 검증하지 않고 존재 여부만 확인)
    }
}

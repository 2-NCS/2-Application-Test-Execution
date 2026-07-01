package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//테스트 환경에서 스프링환경과 Bean 들을 주입해서 사용하기 위해 @SpringBootTest 사용 하며
//MVC 컨트롤러를 테스트를 하기위해 MockMvc를 자동으로 생성하기 위해 @AutoConfigureMockMvc tkdud
@SpringBootTest
@AutoConfigureMockMvc
class LibraryIntegrationTest {

    //MockMvc, BookRepository 주입
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test   //테스트
    @DisplayName("대출하면 재고가 0 이 되고 조회 API 에 반영된다")   //테스트할 때 화면에 표시될 이름
    void 대출_재고반영_흐름() throws Exception {    //모든 예외는 던짐
        Book seed = new Book(); // 책 객체 생성
        seed.setTitle("통합테스트 도서"); //책 이름 설정
        seed.setIsbn("INT-1");  //책 식별기호 설정
        seed.setTotalCopies(1); //책 전체 재고 설정
        seed.setAvailableCopies(1); //책 이용가능 재고 설정
        Long bookId = bookRepository.save(seed).getId();    //생성된 책 객체 DB에 저장후 책 고유번호 Long타입으로 변수에 저장

        String lendJson = "{\"bookId\":" + bookId + ",\"member\":\"hong\"}"; //책 대여 테스트를 위한 JSON형태로 이전 저장한 bookId와 hong이라는 이름으로 String 타입에 저장
        mockMvc.perform(post("/api/library/lend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lendJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lendId").exists());

        mockMvc.perform(get("/api/library/books/" + bookId + "/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));
    }
}

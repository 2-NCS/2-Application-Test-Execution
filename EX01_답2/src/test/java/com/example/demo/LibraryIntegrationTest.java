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

    @Test
    @DisplayName("대출하면 재고가 0 이 되고 조회 API 에 반영된다")
    void 대출_재고반영_흐름() throws Exception {
        Book seed = new Book();
        seed.setTitle("통합테스트 도서");
        seed.setIsbn("INT-1");
        seed.setTotalCopies(1);
        seed.setAvailableCopies(1);
        Long bookId = bookRepository.save(seed).getId();

        String lendJson = "{\"bookId\":" + bookId + ",\"member\":\"hong\"}";
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

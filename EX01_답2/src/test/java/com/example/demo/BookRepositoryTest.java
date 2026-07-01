package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Jpa테스트 관련 설정 불러오기
class BookRepositoryTest {

    @Autowired // 자동연결
    private BookRepository bookRepository; // 테스트할 BookRepository 불러오기

    @Test // 테스트 메서드 지정
    @DisplayName("ISBN 으로 도서를 조회하고 존재 여부를 확인한다") // 테스트 이름을 지정
    void findByIsbn_existsByIsbn() {
        Book seed = new Book();      // Book 엔티티 seed 생성
        seed.setTitle("리팩터링");     // seed.title에 "리팩터링" 삽입
        seed.setIsbn("REPO-1");      // seed.Isbn 에 "REPO-1" 삽입
        seed.setTotalCopies(2);      // seed.totalCopies 에 정수 2 삽입
        seed.setAvailableCopies(2);  // seed.availableCopies 에 정수 2 삽입
        bookRepository.save(seed);   // 작성한 엔티티 db에 저장

//        Optional<Book> hit = bookRepository.findByIsbn("REPO-1");   // db에서 Isbn = "REPO-1" 인 행이 있는지 찾아 hit에 담는다
//
//        assertAll(  // 여러 검증을 모두 실행하고 한 번에 결과를 확인
//                () -> assertTrue(hit.isPresent()),  // hit 에 값이 존재하는가, 있다면 통과
//                () -> assertEquals("리팩터링", hit.orElseThrow().getTitle()), // hit.title의 값이 "리팩터링" 과 같은가, 같다면 통과
//                () -> assertTrue(bookRepository.existsByIsbn("REPO-1")), // db에 저장된 Isbn의 값 중에 "REPO-1" 이 존재하는가, 있다면 통과
//                () -> assertFalse(bookRepository.existsByIsbn("NONE-0")) // db에 저장된 Isbn의 값 중에 "NONE-0" 가 존재하는가, 없다면 통과
//        );
    }
}

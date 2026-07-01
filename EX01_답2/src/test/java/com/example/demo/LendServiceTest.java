package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Repository.BookRepository;
import com.example.demo.Domain.Common.Repository.LendRepository;
import com.example.demo.Domain.Common.Service.LibraryServiceImpl;
import com.example.demo.Exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Mockito를 사용하여 모의 객체(Mock) 환경에서 서비스 로직만 분리해 검증
@ExtendWith(MockitoExtension.class)
class LendServiceTest {

    @Mock
    private BookRepository bookRepository; // 도서 조회를 위한 모의 리포지토리

    @Mock
    private LendRepository lendRepository; // 대출 기록 저장을 위한 모의 리포지토리

    @InjectMocks
    private LibraryServiceImpl service; // 모의 객체들이 주입된 실제 테스트 대상 서비스

    // 테스트용 도서(Book) 객체를 빠르게 생성하기 위한 메서드
    private Book stockBook(Long id, int available) {
        Book b = new Book();
        b.setId(id);
        b.setAvailableCopies(available);
        return b;
    }

    // 도서 정상 대출 시 재고 감소 및 반납기한 설정 테스트 추가
    @Test
    @DisplayName("대출 정상: 재고가 1 줄고 반납기한이 설정된다")
    void lend_정상() {
        // Given: ID가 1이고 재고가 2권인 도서 데이터와 가짜 리포지토리 행동 정의
        Book book = stockBook(1L, 2);
        book.setTitle("클린 코드");
        book.setIsbn("i-1");
        book.setTotalCopies(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: 대출 로직 실행
        Lend created = service.lend(1L, "kim");

        // Then: 결과 검증 (재고가 1권으로 줄고, 반납 기한이 생겼으며, 대출자가 맞는지 확인)
        assertAll(
                () -> assertEquals(1, book.getAvailableCopies()),
                () -> assertNotNull(created.getDueAt()),
                () -> assertEquals("kim", created.getMember())
        );
    }

    // 대출 경계값(재고 0)일 때 대출 불가 예외 테스트 추가
    @Test
    @DisplayName("대출 경계: 재고 0 이면 대출할 수 없다 (D1 경계값)")
    void lend_재고0_예외() {
        // Given: 재고가 0권인 도서 상태 설정
        when(bookRepository.findById(1L)).thenReturn(Optional.of(stockBook(1L, 0)));

        // // When & Then: 대출 시도 시 BizException 예외가 터지는지 확인
        assertThrows(BizException.class, () -> service.lend(1L, "kim"));
    }

    // 존재하지 않는 도서 ID로 대출 시 예외 발생 테스트 추가
    @Test
    @DisplayName("대출 예외: 존재하지 않는 도서면 BizException (D2)")
    void lend_미존재도서_예외() {
        // Given: 존재하지 않는 도서 ID(99번) 조회 시 빈 값(Optional.empty) 반환 설정
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then: 예외가 터지는지 확인하고, 에러 메시지가 존재하는지 검증
        BizException ex = assertThrows(BizException.class, () -> service.lend(99L, "kim"));
        assertNotNull(ex.getMessage());
    }

    // 동일 도서 중복 반납 차단 및 재고 정상 유지 테스트 추가
    @Test
    @DisplayName("반납 예외: 두 번 반납해도 재고는 한 번만 늘고 두 번째는 BizException (D3 중복 반납)")
    void returnBook_중복반납_차단() {
        // Given: 초기 대출 상태 및 재고가 1권인 도서 설정
        Book book = stockBook(1L, 1);
        Lend lend = new Lend();
        lend.setId(1L);
        lend.setBookId(1L);
        when(lendRepository.findById(1L)).thenReturn(Optional.of(lend));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: 첫 번째 정상 반납 처리 (재고가 1 -> 2로 증가)
        service.returnBook(1L);

        // Then: 동일한 대출 건으로 연속 반납 시 예외가 발생하는지, 재고는 여전히 2권인지 검증
        assertAll(
                () -> assertThrows(BizException.class, () -> service.returnBook(1L)),
                () -> assertEquals(2, book.getAvailableCopies())
        );
    }

    // 반납기한 정각 및 초 단위 연체 기준 경계값 테스트 추가
    @Test
    @DisplayName("연체 경계: 반납기한 당일은 연체가 아니고 그 이후가 연체다 (D4 경계값)")
    void isOverdue_경계() {
        // Given: 기준 반납 기한 설정
        LocalDateTime due = LocalDateTime.of(2026, 1, 15, 10, 0);

        // 미반납 상태의 대출 데이터
        Lend open = new Lend();
        open.setDueAt(due);

        // 기한 이틀 전에 이미 반납 완료한 대출 데이터
        Lend done = new Lend();
        done.setDueAt(due);
        done.setReturnedAt(due.minusDays(2));

        // When & Then: 초 단위 경계값 기준에 따른 연체 여부(True/False) 검증
        assertAll(
                () -> assertFalse(service.isOverdue(open, due.minusDays(1))),
                () -> assertFalse(service.isOverdue(open, due)),
                () -> assertTrue(service.isOverdue(open, due.plusSeconds(1))),
                () -> assertFalse(service.isOverdue(done, due.plusDays(10)))
        );
    }
}

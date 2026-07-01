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

@ExtendWith(MockitoExtension.class)
class LendServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LendRepository lendRepository;

    @InjectMocks
    private LibraryServiceImpl service;

    private Book stockBook(Long id, int available) {
        Book b = new Book();
        b.setId(id);
        b.setAvailableCopies(available);
        return b;
    }

    @Test
    @DisplayName("대출 정상: 재고가 1 줄고 반납기한이 설정된다")
    void lend_정상() {
        Book book = stockBook(1L, 2);
        book.setTitle("클린 코드");
        book.setIsbn("i-1");
        book.setTotalCopies(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        Lend created = service.lend(1L, "kim");

        assertAll(
                () -> assertEquals(1, book.getAvailableCopies()),
                () -> assertNotNull(created.getDueAt()),
                () -> assertEquals("kim", created.getMember())
        );
    }

    @Test
    @DisplayName("대출 경계: 재고 0 이면 대출할 수 없다 (D1 경계값)")
    void lend_재고0_예외() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(stockBook(1L, 0)));

        assertThrows(BizException.class, () -> service.lend(1L, "kim"));
    }

    @Test
    @DisplayName("대출 예외: 존재하지 않는 도서면 BizException (D2)")
    void lend_미존재도서_예외() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BizException ex = assertThrows(BizException.class, () -> service.lend(99L, "kim"));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("반납 예외: 두 번 반납해도 재고는 한 번만 늘고 두 번째는 BizException (D3 중복 반납)")
    void returnBook_중복반납_차단() {
        Book book = stockBook(1L, 1);
        Lend lend = new Lend();
        lend.setId(1L);
        lend.setBookId(1L);
        when(lendRepository.findById(1L)).thenReturn(Optional.of(lend));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(lendRepository.save(any(Lend.class))).thenAnswer(inv -> inv.getArgument(0));

        service.returnBook(1L);

        assertAll(
                () -> assertThrows(BizException.class, () -> service.returnBook(1L)),
                () -> assertEquals(2, book.getAvailableCopies())
        );
    }

    @Test
    @DisplayName("연체 경계: 반납기한 당일은 연체가 아니고 그 이후가 연체다 (D4 경계값)")
    void isOverdue_경계() {
        LocalDateTime due = LocalDateTime.of(2026, 1, 15, 10, 0);
        Lend open = new Lend();
        open.setDueAt(due);

        Lend done = new Lend();
        done.setDueAt(due);
        done.setReturnedAt(due.minusDays(2));

        assertAll(
                () -> assertFalse(service.isOverdue(open, due.minusDays(1))),
                () -> assertFalse(service.isOverdue(open, due)),
                () -> assertTrue(service.isOverdue(open, due.plusSeconds(1))),
                () -> assertFalse(service.isOverdue(done, due.plusDays(10)))
        );
    }
}

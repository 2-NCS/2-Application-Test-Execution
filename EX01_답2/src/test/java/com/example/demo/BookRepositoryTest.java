package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("ISBN 으로 도서를 조회하고 존재 여부를 확인한다")
    void findByIsbn_existsByIsbn() {
        Book seed = new Book();
        seed.setTitle("리팩터링");
        seed.setIsbn("REPO-1");
        seed.setTotalCopies(2);
        seed.setAvailableCopies(2);
        bookRepository.save(seed);

        Optional<Book> hit = bookRepository.findByIsbn("REPO-1");

        assertAll(
                () -> assertTrue(hit.isPresent()),
                () -> assertEquals("리팩터링", hit.orElseThrow().getTitle()),
                () -> assertTrue(bookRepository.existsByIsbn("REPO-1")),
                () -> assertFalse(bookRepository.existsByIsbn("NONE-0"))
        );
    }
}

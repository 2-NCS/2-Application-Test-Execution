package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Repository.BookRepository;
import com.example.demo.Domain.Common.Repository.LendRepository;
import com.example.demo.Exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LibraryServiceImpl implements LibraryService {

    public static final int LOAN_DAYS = 14;

    private final BookRepository bookRepository;
    private final LendRepository lendRepository;

    public LibraryServiceImpl(BookRepository bookRepository, LendRepository lendRepository) {
        this.bookRepository = bookRepository;
        this.lendRepository = lendRepository;
    }

    private Book mustGetBook(Long bookId) {
        Optional<Book> found = bookRepository.findById(bookId);
        if (found.isEmpty()) {
            throw new BizException("존재하지 않는 도서입니다. id=" + bookId);
        }
        return found.get();
    }

    @Override
    @Transactional
    public Lend lend(Long bookId, String member) {
        Book target = mustGetBook(bookId);

        int remaining = target.getAvailableCopies();
        if (remaining < 1) {
            throw new BizException("대출 가능한 재고가 없습니다.");
        }

        target.setAvailableCopies(remaining - 1);
        bookRepository.save(target);

        LocalDateTime borrowedAt = LocalDateTime.now();
        Lend record = new Lend();
        record.setBookId(bookId);
        record.setMember(member);
        record.setLentAt(borrowedAt);
        record.setDueAt(borrowedAt.plusDays(LOAN_DAYS));
        return lendRepository.save(record);
    }

    @Override
    @Transactional
    public Lend returnBook(Long lendId) {
        Lend record = lendRepository.findById(lendId)
                .orElseThrow(() -> new BizException("존재하지 않는 대출 기록입니다. id=" + lendId));

        boolean alreadyReturned = record.getReturnedAt() != null;
        if (alreadyReturned) {
            throw new BizException("이미 반납된 대출입니다.");
        }

        record.setReturnedAt(LocalDateTime.now());
        lendRepository.save(record);

        Book target = mustGetBook(record.getBookId());
        target.setAvailableCopies(target.getAvailableCopies() + 1);
        bookRepository.save(target);

        return record;
    }

    @Override
    public boolean isOverdue(Lend lend, LocalDateTime asOf) {
        if (lend.getReturnedAt() != null) {
            return false;
        }
        return lend.getDueAt().isBefore(asOf);
    }

    @Override
    @Transactional(readOnly = true)
    public int availableCopies(Long bookId) {
        return mustGetBook(bookId).getAvailableCopies();
    }
}

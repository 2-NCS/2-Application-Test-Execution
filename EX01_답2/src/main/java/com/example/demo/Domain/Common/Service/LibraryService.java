package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Lend;

import java.time.LocalDateTime;

public interface LibraryService {

    Lend lend(Long bookId, String member);

    Lend returnBook(Long lendId);

    boolean isOverdue(Lend lend, LocalDateTime asOf);

    int availableCopies(Long bookId);
}

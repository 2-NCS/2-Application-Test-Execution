package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.LendRequest;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/lend")
    public ResponseEntity<Map<String, Object>> lend(@RequestBody @Valid LendRequest req) {
        Lend result = libraryService.lend(req.getBookId(), req.getMember());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dueAt", result.getDueAt());
        payload.put("lendId", result.getId());
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }

    @PostMapping("/return/{lendId}")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable("lendId") Long lendId) {
        Lend result = libraryService.returnBook(lendId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("returnedAt", result.getReturnedAt());
        payload.put("lendId", result.getId());
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }

    @GetMapping("/books/{id}/available")
    public ResponseEntity<Map<String, Object>> available(@PathVariable("id") Long id) {
        int copies = libraryService.availableCopies(id);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("availableCopies", copies);
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }
}

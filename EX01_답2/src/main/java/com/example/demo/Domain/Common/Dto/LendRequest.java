package com.example.demo.Domain.Common.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LendRequest {

    @NotNull(message = "도서 id는 필수입니다.")
    private Long bookId;

    @NotBlank(message = "대출자(member)는 필수입니다.")
    private String member;
}

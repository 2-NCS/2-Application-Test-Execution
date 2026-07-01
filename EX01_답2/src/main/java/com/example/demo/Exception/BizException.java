package com.example.demo.Exception;

/**
 * 비즈니스 규칙 위반을 나타내는 예외다.
 * 전역 예외 핸들러에서 400(Bad Request)으로 변환한다.
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}

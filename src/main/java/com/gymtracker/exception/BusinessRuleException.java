package com.gymtracker.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends AppException {

    public BusinessRuleException(ErrorCode code, Object... args) {
        super(code, args);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}

package com.gymtracker.exception;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {
    private final ErrorCode code;

    protected AppException(ErrorCode code, Object... args) {
        super(code.format(args));
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public abstract HttpStatus getStatus();
}

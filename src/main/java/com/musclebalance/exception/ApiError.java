package com.musclebalance.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status,
                       String code,
                       String message,
                       String path,
                       Instant timestamp,
                       List<FieldViolation> fieldErrors) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiError of(int status, ErrorCode code, String message, String path) {
        return new ApiError(status, code.name(), message, path, Instant.now(), null);
    }

    public static ApiError validation(int status, List<FieldViolation> fieldErrors, String path) {
        return new ApiError(status, CommonErrorCode.VALIDATION_FAILED.name(),
                CommonErrorCode.VALIDATION_FAILED.format(), path, Instant.now(), List.copyOf(fieldErrors));
    }

    public static ApiError internal(String path) {
        return new ApiError(500, CommonErrorCode.INTERNAL_ERROR.name(),
                CommonErrorCode.INTERNAL_ERROR.format(), path, Instant.now(), null);
    }
}

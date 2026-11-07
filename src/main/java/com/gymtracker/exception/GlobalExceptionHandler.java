package com.gymtracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleApp(AppException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        log.warn("{} {} -> {} {}: {}", request.getMethod(), request.getRequestURI(),
                status.value(), ex.getCode().name(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        log.warn("{} {} -> 409 DATA_CONFLICT: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, CommonErrorCode.DATA_CONFLICT, CommonErrorCode.DATA_CONFLICT.format(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("{} {} -> 500", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.internal(request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<ApiError.FieldViolation> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> violations.add(new ApiError.FieldViolation(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> violations.add(new ApiError.FieldViolation(e.getObjectName(), e.getDefaultMessage())));
        return validation(violations, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<ApiError.FieldViolation> violations = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                violations.add(new ApiError.FieldViolation(name, error.getDefaultMessage()));
            }
        });
        return validation(violations, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String path = path(request);
        int status = statusCode.value();
        ApiError error;
        if (status >= 500) {
            log.error("{} -> {}", path, status, ex);
            error = ApiError.internal(path);
        } else {
            ErrorCode code = codeFor(ex);
            String message = messageFor(ex, code);
            log.warn("{} -> {} {}: {}", path, status, code.name(), ex.getMessage());
            error = ApiError.of(status, code, message, path);
        }
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    private ResponseEntity<Object> validation(List<ApiError.FieldViolation> violations, HttpHeaders headers,
                                              HttpStatusCode status, WebRequest request) {
        String path = path(request);
        log.warn("{} -> {} VALIDATION_FAILED: {}", path, status.value(), violations);
        return ResponseEntity.status(status).headers(headers)
                .body(ApiError.validation(status.value(), violations, path));
    }

    private static ErrorCode codeFor(Exception ex) {
        return switch (ex) {
            case HttpMessageNotReadableException e -> CommonErrorCode.MALFORMED_REQUEST;
            case TypeMismatchException e -> CommonErrorCode.INVALID_PARAMETER;
            case MissingServletRequestParameterException e -> CommonErrorCode.MISSING_PARAMETER;
            case MissingPathVariableException e -> CommonErrorCode.MISSING_PARAMETER;
            case HttpRequestMethodNotSupportedException e -> CommonErrorCode.METHOD_NOT_ALLOWED;
            case HttpMediaTypeNotSupportedException e -> CommonErrorCode.UNSUPPORTED_MEDIA_TYPE;
            case HttpMediaTypeNotAcceptableException e -> CommonErrorCode.NOT_ACCEPTABLE;
            case NoResourceFoundException e -> CommonErrorCode.ENDPOINT_NOT_FOUND;
            case NoHandlerFoundException e -> CommonErrorCode.ENDPOINT_NOT_FOUND;
            default -> CommonErrorCode.BAD_REQUEST;
        };
    }

    private static String messageFor(Exception ex, ErrorCode code) {
        return switch (ex) {
            case TypeMismatchException e -> code.format(e.getPropertyName());
            case MissingServletRequestParameterException e -> code.format(e.getParameterName());
            case MissingPathVariableException e -> code.format(e.getVariableName());
            case HttpRequestMethodNotSupportedException e -> code.format(e.getMethod());
            default -> code.format();
        };
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : null;
    }
}

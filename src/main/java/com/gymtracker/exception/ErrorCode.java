package com.gymtracker.exception;

public interface ErrorCode {

    String name();

    String template();

    default String format(Object... args) {
        return args.length == 0 ? template() : String.format(template(), args);
    }
}

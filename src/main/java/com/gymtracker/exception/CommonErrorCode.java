package com.gymtracker.exception;

public enum CommonErrorCode implements ErrorCode {
    VALIDATION_FAILED("Проверьте заполнение полей"),
    MALFORMED_REQUEST("Не удалось прочитать тело запроса"),
    INVALID_PARAMETER("Неверное значение параметра «%s»"),
    MISSING_PARAMETER("Не указан параметр «%s»"),
    BAD_REQUEST("Некорректный запрос"),
    ENDPOINT_NOT_FOUND("Такого адреса нет"),
    METHOD_NOT_ALLOWED("Метод %s для этого адреса не поддерживается"),
    UNSUPPORTED_MEDIA_TYPE("Неподдерживаемый формат тела запроса"),
    NOT_ACCEPTABLE("Запрошенный формат ответа не поддерживается"),
    DATA_CONFLICT("Данные конфликтуют с уже сохранёнными"),
    INTERNAL_ERROR("Что-то пошло не так, попробуйте ещё раз");

    private final String template;

    CommonErrorCode(String template) {
        this.template = template;
    }

    @Override
    public String template() {
        return template;
    }
}

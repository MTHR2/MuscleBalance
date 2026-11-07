package com.gymtracker.exception;

public enum DomainErrorCode implements ErrorCode {
    EXERCISE_NOT_FOUND("Упражнение не найдено"),
    EXERCISE_NAME_TAKEN("Упражнение «%s» уже есть"),
    EXERCISE_MUSCLE_DUPLICATED("Мышца указана дважды"),
    EXERCISE_PRIMARY_MUSCLE_REQUIRED("Нужна хотя бы одна основная мышца"),
    EXERCISES_NOT_FOUND("Одно или несколько упражнений не найдены"),

    MUSCLES_NOT_FOUND("Одна или несколько мышц не найдены"),
    MUSCLE_GROUP_NOT_FOUND("Группа мышц не найдена"),

    TEMPLATE_NOT_FOUND("Шаблон тренировки не найден"),
    TEMPLATE_NAME_TAKEN("Шаблон «%s» уже есть"),
    TEMPLATE_EXERCISE_DUPLICATED("Упражнение добавлено в шаблон дважды"),
    TEMPLATES_NOT_FOUND("Один или несколько шаблонов тренировок не найдены"),

    PLAN_NOT_FOUND("План не найден"),
    PLAN_NAME_TAKEN("План «%s» уже есть"),
    PLAN_DAY_DUPLICATED("На один день недели указано две тренировки"),
    PLAN_MUSCLE_TARGET_DUPLICATED("На одну мышцу указано две цели"),

    WORKOUT_NOT_FOUND("Тренировка не найдена"),
    WORKOUT_ALREADY_ACTIVE("Уже идёт другая тренировка: завершите или отмените её"),
    WORKOUT_ALREADY_FINISHED("Тренировка уже завершена"),
    WORKOUT_NO_WORKING_SETS("В тренировке нет ни одного рабочего подхода, отмените её"),
    WORKOUT_FILTER_UNSUPPORTED("Поддерживается только finished=true"),
    WORKOUT_EXERCISE_NOT_FOUND("Упражнение не найдено в тренировке"),
    WORKOUT_EXERCISE_DUPLICATED("Это упражнение уже есть в тренировке"),
    WORKOUT_SET_NOT_FOUND("Подход не найден"),
    WORKOUT_SET_NOT_LAST("Удалить можно только последний подход"),

    BODY_WEIGHT_NOT_FOUND("Замер веса не найден");

    private final String template;

    DomainErrorCode(String template) {
        this.template = template;
    }

    @Override
    public String template() {
        return template;
    }
}

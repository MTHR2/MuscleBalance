package com.musclebalance.exception;

import org.springframework.http.HttpStatus;

import static com.musclebalance.exception.DomainErrorCode.*;

public class ConflictException extends AppException {

    public ConflictException(ErrorCode code, Object... args) {
        super(code, args);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    public static ConflictException exerciseNameTaken(String name) {
        return new ConflictException(EXERCISE_NAME_TAKEN, name);
    }

    public static ConflictException templateNameTaken(String name) {
        return new ConflictException(TEMPLATE_NAME_TAKEN, name);
    }

    public static ConflictException planNameTaken(String name) {
        return new ConflictException(PLAN_NAME_TAKEN, name);
    }

    public static ConflictException workoutAlreadyActive() {
        return new ConflictException(WORKOUT_ALREADY_ACTIVE);
    }

    public static ConflictException workoutAlreadyFinished() {
        return new ConflictException(WORKOUT_ALREADY_FINISHED);
    }

    public static ConflictException workoutExerciseDuplicated() {
        return new ConflictException(WORKOUT_EXERCISE_DUPLICATED);
    }

    public static ConflictException setNotLast() {
        return new ConflictException(WORKOUT_SET_NOT_LAST);
    }
}

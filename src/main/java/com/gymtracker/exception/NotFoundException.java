package com.gymtracker.exception;

import org.springframework.http.HttpStatus;

import static com.gymtracker.exception.DomainErrorCode.*;

public class NotFoundException extends AppException {

    public NotFoundException(ErrorCode code, Object... args) {
        super(code, args);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    public static NotFoundException exercise() {
        return new NotFoundException(EXERCISE_NOT_FOUND);
    }

    public static NotFoundException muscleGroup() {
        return new NotFoundException(MUSCLE_GROUP_NOT_FOUND);
    }

    public static NotFoundException template() {
        return new NotFoundException(TEMPLATE_NOT_FOUND);
    }

    public static NotFoundException plan() {
        return new NotFoundException(PLAN_NOT_FOUND);
    }

    public static NotFoundException workout() {
        return new NotFoundException(WORKOUT_NOT_FOUND);
    }

    public static NotFoundException workoutExercise() {
        return new NotFoundException(WORKOUT_EXERCISE_NOT_FOUND);
    }

    public static NotFoundException workoutSet() {
        return new NotFoundException(WORKOUT_SET_NOT_FOUND);
    }

    public static NotFoundException bodyWeight() {
        return new NotFoundException(BODY_WEIGHT_NOT_FOUND);
    }
}

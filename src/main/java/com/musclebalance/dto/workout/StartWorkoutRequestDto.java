package com.musclebalance.dto.workout;

import jakarta.validation.constraints.Size;

/** workoutTemplateId = null: свободная тренировка; name используется только для неё */
public record StartWorkoutRequestDto(Long workoutTemplateId,
                                     @Size(max = 100) String name) {
}

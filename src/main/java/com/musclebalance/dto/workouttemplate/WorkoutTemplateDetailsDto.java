package com.musclebalance.dto.workouttemplate;

import java.util.List;

public record WorkoutTemplateDetailsDto(
        Long id,
        String name,
        List<WorkoutTemplateExerciseDto> exercises

) {
}

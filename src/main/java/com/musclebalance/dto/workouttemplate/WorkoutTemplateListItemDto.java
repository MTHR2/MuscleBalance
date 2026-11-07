package com.musclebalance.dto.workouttemplate;

public record WorkoutTemplateListItemDto(
        Long id,
        String name,
        int exerciseCount
) {
}

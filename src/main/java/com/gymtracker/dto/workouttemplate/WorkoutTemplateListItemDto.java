package com.gymtracker.dto.workouttemplate;

public record WorkoutTemplateListItemDto(
        Long id,
        String name,
        int exerciseCount
) {
}

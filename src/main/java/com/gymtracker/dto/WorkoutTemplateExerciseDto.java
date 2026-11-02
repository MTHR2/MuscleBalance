package com.gymtracker.dto;

public record WorkoutTemplateExerciseDto(
        Long exerciseId,
        String exerciseName,
        Short sortOrder,
        Short targetSets,
        Short repsMin,
        Short repsMax
) {
}

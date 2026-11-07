package com.musclebalance.dto.workouttemplate;

public record WorkoutTemplateExerciseDto(
        Long exerciseId,
        String exerciseName,
        String imageUrl,
        Short sortOrder,
        Short targetSets,
        Short repsMin,
        Short repsMax
) {
}

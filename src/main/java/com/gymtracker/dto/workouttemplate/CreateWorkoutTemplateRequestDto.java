package com.gymtracker.dto.workouttemplate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateWorkoutTemplateRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotEmpty @Size(max = 20) @Valid List<WorkoutTemplateExerciseRequestDto> exercises
) {
}

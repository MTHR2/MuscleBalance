package com.gymtracker.dto.exercise;

import com.gymtracker.entity.LoadType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateExerciseRequestDto(@NotBlank @Size(max = 100) String name,
                                       @Size(max = 1000) String description,
                                       @NotNull LoadType loadType,
                                       @Valid @NotEmpty @Size(max = 30) List<ExerciseMuscleRequestDto> muscles) {
}

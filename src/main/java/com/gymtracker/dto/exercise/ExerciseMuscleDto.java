package com.gymtracker.dto.exercise;

import com.gymtracker.entity.MuscleRole;


public record ExerciseMuscleDto(Short muscleId,
                                String muscleName,
                                MuscleRole role) {
}

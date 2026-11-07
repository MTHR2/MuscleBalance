package com.musclebalance.dto.exercise;

import com.musclebalance.entity.MuscleRole;


public record ExerciseMuscleDto(Short muscleId,
                                String muscleName,
                                MuscleRole role) {
}

package com.gymtracker.dto;

import com.gymtracker.entity.MuscleRole;


public record ExerciseMuscleDto(Short muscleId,
                                String muscleName,
                                MuscleRole role) {
}

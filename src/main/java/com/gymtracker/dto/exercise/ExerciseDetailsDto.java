package com.gymtracker.dto.exercise;

import com.gymtracker.entity.LoadType;

import java.math.BigDecimal;
import java.util.List;

public record ExerciseDetailsDto(Long id,
                                 String name,
                                 String description,
                                 String imageUrl,
                                 LoadType loadType,
                                 boolean custom,
                                 boolean favourite,
                                 BigDecimal workingWeight,
                                 List<ExerciseMuscleDto> muscles) {
}

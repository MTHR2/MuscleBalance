package com.musclebalance.dto.exercise;

import com.musclebalance.entity.LoadType;

import java.math.BigDecimal;
import java.util.List;

public record ExerciseListItemDto(Long id,
                                  String name,
                                  String imageUrl,
                                  LoadType loadType,
                                  boolean favourite,
                                  boolean custom,
                                  BigDecimal workingWeight,
                                  List<ExerciseMuscleDto> muscles) {
}

package com.gymtracker.dto.exercise;

import com.gymtracker.entity.LoadType;

import java.util.List;

public record ExerciseListItemDto(Long id,
                                  String name,
                                  String imageUrl,
                                  LoadType loadType,
                                  boolean favourite,
                                  boolean custom,
                                  List<ExerciseMuscleDto> muscles) {
}

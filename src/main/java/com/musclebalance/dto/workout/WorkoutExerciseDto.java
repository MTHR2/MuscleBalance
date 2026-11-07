package com.musclebalance.dto.workout;

import com.musclebalance.entity.LoadType;

import java.math.BigDecimal;
import java.util.List;

public record WorkoutExerciseDto (Long id,
                                  Long exerciseId,
                                  String exerciseName,
                                  String imageUrl,
                                  LoadType loadType,
                                  Short sortOrder,
                                  Short plannedSets,
                                  Short targetSets,
                                  Short repsMin,
                                  Short repsMax,
                                  BigDecimal workingWeight,
                                  List<WorkoutSetDto> sets){
}

package com.musclebalance.dto.stats;

public record MuscleGroupStatsDto(Short groupId,
                                  String name,
                                  double sets,
                                  int targetSets) {
}

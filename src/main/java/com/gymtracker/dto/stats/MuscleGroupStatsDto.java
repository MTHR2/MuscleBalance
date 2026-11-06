package com.gymtracker.dto.stats;

public record MuscleGroupStatsDto(Short groupId,
                                  String name,
                                  double sets,
                                  int targetSets) {
}

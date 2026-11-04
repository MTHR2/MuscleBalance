package com.gymtracker.dto.stats;

public record MuscleStatsDto(Short muscleId,
                             String name,
                             String regionKey,
                             Short groupId,
                             String groupName,
                             double sets,
                             double reps,
                             Short targetSets) {
}

package com.gymtracker.dto.stats;

public record StatsOverviewDto(int workoutsThisMonth,
                               Integer workoutsPlannedThisMonth,
                               Integer weekStreak,
                               double volumeTonnes30d,
                               double volumeTonnesPrev30d,
                               Integer volumeChangePercent,
                               Long avgDurationSeconds,
                               Double avgSets) {
}

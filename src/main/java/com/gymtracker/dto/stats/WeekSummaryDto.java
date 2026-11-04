package com.gymtracker.dto.stats;

public record WeekSummaryDto(int workoutsDone,
                             Integer workoutsPlanned,
                             int workingSets,
                             Integer targetPercent) {
}

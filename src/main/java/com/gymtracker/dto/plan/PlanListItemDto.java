package com.gymtracker.dto.plan;

public record PlanListItemDto(Long id,
                              String name,
                              boolean active,
                              int workoutCount) {
}

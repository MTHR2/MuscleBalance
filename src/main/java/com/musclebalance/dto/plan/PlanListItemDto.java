package com.musclebalance.dto.plan;

public record PlanListItemDto(Long id,
                              String name,
                              boolean active,
                              int workoutCount) {
}

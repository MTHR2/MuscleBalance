package com.gymtracker.dto;

public record PlanListItemDto(Long id,
                              String name,
                              boolean active,
                              int workoutCount) {
}

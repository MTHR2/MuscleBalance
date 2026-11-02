package com.gymtracker.dto;

public record WorkoutTemplateListItemDto(
        Long id,
        String name,
        int exerciseCount
) {
}

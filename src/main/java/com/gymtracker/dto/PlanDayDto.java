package com.gymtracker.dto;

public record PlanDayDto(Short dayOfWeek,
                         Long workoutTemplateId,
                         String workoutTemplateName) {
}

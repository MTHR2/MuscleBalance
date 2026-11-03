package com.gymtracker.dto.plan;

public record PlanDayDto(Short dayOfWeek,
                         Long workoutTemplateId,
                         String workoutTemplateName) {
}

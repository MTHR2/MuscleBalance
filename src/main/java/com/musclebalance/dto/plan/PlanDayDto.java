package com.musclebalance.dto.plan;

public record PlanDayDto(Short dayOfWeek,
                         Long workoutTemplateId,
                         String workoutTemplateName) {
}

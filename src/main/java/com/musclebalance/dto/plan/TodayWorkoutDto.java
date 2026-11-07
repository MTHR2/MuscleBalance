package com.musclebalance.dto.plan;

import com.musclebalance.dto.workouttemplate.WorkoutTemplateExerciseDto;

import java.util.List;

/** Тренировка активного плана на сегодня; completedToday = по этому шаблону сегодня уже завершена тренировка */
public record TodayWorkoutDto(Long id,
                              String name,
                              List<WorkoutTemplateExerciseDto> exercises,
                              boolean completedToday) {
}

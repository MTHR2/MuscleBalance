package com.musclebalance.dto.stats;

import com.musclebalance.entity.MuscleRole;

/** Строка агрегата из БД: рабочие подходы и повторы на мышцу в одной роли (без коэффициентов) */
public record MuscleLoadRow(Short muscleId,
                            MuscleRole role,
                            long sets,
                            long reps) {
}

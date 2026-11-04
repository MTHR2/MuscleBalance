package com.gymtracker.dto.stats;

import com.gymtracker.entity.MuscleRole;

/** Строка агрегата из БД: рабочие подходы и повторы на мышцу в одной роли (без коэффициентов) */
public record MuscleLoadRow(Short muscleId,
                            MuscleRole role,
                            long sets,
                            long reps) {
}

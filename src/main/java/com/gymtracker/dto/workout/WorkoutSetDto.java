package com.gymtracker.dto.workout;


import java.math.BigDecimal;

public record WorkoutSetDto(Long id,
                            Short setNumber,
                            Short reps,
                            BigDecimal weight,
                            boolean warmup) {
}

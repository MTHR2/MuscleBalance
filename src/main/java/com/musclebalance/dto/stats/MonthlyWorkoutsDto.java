package com.musclebalance.dto.stats;

import java.time.YearMonth;

public record MonthlyWorkoutsDto(YearMonth month,
                                 int count,
                                 Integer planned) {
}

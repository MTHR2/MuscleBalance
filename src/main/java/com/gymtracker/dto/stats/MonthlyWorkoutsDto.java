package com.gymtracker.dto.stats;

import java.time.YearMonth;

public record MonthlyWorkoutsDto(YearMonth month,
                                 int count,
                                 Integer planned) {
}

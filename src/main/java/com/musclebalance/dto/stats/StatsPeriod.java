package com.musclebalance.dto.stats;

public enum StatsPeriod {
    MONTH(1),
    QUARTER(3),
    YEAR(12);

    private final int months;

    StatsPeriod(int months) {
        this.months = months;
    }

    public int months() {
        return months;
    }
}

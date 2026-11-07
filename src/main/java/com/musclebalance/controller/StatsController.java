package com.musclebalance.controller;

import com.musclebalance.dto.stats.ExerciseProgressDto;
import com.musclebalance.dto.stats.ExerciseStatsItemDto;
import com.musclebalance.dto.stats.MonthlyWorkoutsDto;
import com.musclebalance.dto.stats.MuscleGroupStatsDto;
import com.musclebalance.dto.stats.MuscleStatsDto;
import com.musclebalance.dto.stats.PersonalRecordsDto;
import com.musclebalance.dto.stats.StatsOverviewDto;
import com.musclebalance.dto.stats.StatsPeriod;
import com.musclebalance.dto.stats.WeekSummaryDto;
import com.musclebalance.service.ProgressService;
import com.musclebalance.service.StatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatsController {
    private final StatsService statsService;
    private final ProgressService progressService;

    @GetMapping("/muscles")
    public List<MuscleStatsDto> getMuscleStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return statsService.getMuscleStats(weekStart);
    }

    @GetMapping("/week")
    public WeekSummaryDto getWeekSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return statsService.getWeekSummary(weekStart);
    }

    @GetMapping("/overview")
    public StatsOverviewDto getOverview(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return statsService.getOverview(period);
    }

    @GetMapping("/muscle-groups")
    public List<MuscleGroupStatsDto> getMuscleGroupStats(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return statsService.getMuscleGroupStats(period);
    }

    @GetMapping("/workouts/monthly")
    public List<MonthlyWorkoutsDto> getMonthlyWorkouts(
            @RequestParam(defaultValue = "12") @Min(1) @Max(60) int months) {
        return statsService.getMonthlyWorkouts(months);
    }

    @GetMapping("/exercises")
    public List<ExerciseStatsItemDto> getExercises(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return progressService.getExercises(period);
    }

    @GetMapping("/exercises/{id}/progress")
    public ExerciseProgressDto getProgress(@PathVariable Long id,
                                           @RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return progressService.getProgress(id, period);
    }

    @GetMapping("/records")
    public PersonalRecordsDto getRecords(@RequestParam(defaultValue = "MONTH") StatsPeriod period,
                                         @RequestParam(defaultValue = "5") @Min(1) @Max(100) int limit) {
        return progressService.getRecords(period, limit);
    }
}

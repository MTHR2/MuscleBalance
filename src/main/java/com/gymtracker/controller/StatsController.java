package com.gymtracker.controller;

import com.gymtracker.dto.stats.MuscleStatsDto;
import com.gymtracker.dto.stats.WeekSummaryDto;
import com.gymtracker.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
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
}

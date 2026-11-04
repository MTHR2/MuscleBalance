package com.gymtracker.service;

import com.gymtracker.dto.stats.MuscleLoadRow;
import com.gymtracker.dto.stats.MuscleStatsDto;
import com.gymtracker.dto.stats.WeekSummaryDto;
import com.gymtracker.entity.Muscle;
import com.gymtracker.entity.MuscleRole;
import com.gymtracker.entity.Plan;
import com.gymtracker.entity.PlanMuscleTarget;
import com.gymtracker.mapper.StatsMapper;
import com.gymtracker.repository.MuscleRepository;
import com.gymtracker.repository.PlanRepository;
import com.gymtracker.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final double SECONDARY_COEFFICIENT = 0.5;
    private static final Short DEFAULT_WEEKLY_SETS = 10;

    private final StatsMapper statsMapper;
    private final WorkoutRepository workoutRepository;
    private final MuscleRepository muscleRepository;
    private final PlanRepository planRepository;


    private record Week(OffsetDateTime from, OffsetDateTime to) {
    }

    private Week resolveWeek(LocalDate weekStart) {
        LocalDate date = weekStart != null ? weekStart : LocalDate.now();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime from = monday.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        return new Week(from, from.plusDays(7));
    }

    private double coefficient(MuscleRole role) {
        return role == MuscleRole.primary ? 1.0 : SECONDARY_COEFFICIENT;
    }


    private Map<Short, Short> muscleTargets(Plan plan) {
        Map<Short, Short> targets = new HashMap<>();
        if (plan == null) {
            return targets;
        }
        for (PlanMuscleTarget target : plan.getMuscleTargets()) {
            targets.put(target.getId().getMuscleId(), target.getWeeklySets());
        }
        return targets;
    }

    private List<MuscleStatsDto> buildMuscleStats(Week week, Plan plan) {
        Map<Short, Double> setsByMuscle = new HashMap<>();
        Map<Short, Double> repsByMuscle = new HashMap<>();
        for (MuscleLoadRow row : workoutRepository.findMuscleLoad(week.from(), week.to())) {
            double k = coefficient(row.role());
            setsByMuscle.merge(row.muscleId(), row.sets() * k, Double::sum);
            repsByMuscle.merge(row.muscleId(), row.reps() * k, Double::sum);
        }

        Map<Short, Short> targets = muscleTargets(plan);
        // нет плана или общей цели: считаем по цели по умолчанию, чтобы карта и процент недели совпадали
        Short defaultTarget = plan != null && plan.getDefaultWeeklySets() != null
                ? plan.getDefaultWeeklySets() : DEFAULT_WEEKLY_SETS;

        List<Muscle> muscles = muscleRepository.findAllByOrderByMuscleGroup_SortOrderAscSortOrderAsc();
        return muscles.stream()
                .map(m -> statsMapper.toMuscleStatsDto(m,
                        setsByMuscle.getOrDefault(m.getId(), 0.0),
                        repsByMuscle.getOrDefault(m.getId(), 0.0),
                        targets.getOrDefault(m.getId(), defaultTarget)))
                .toList();
    }

    private Integer calculateTargetPercent(List<MuscleStatsDto> stats) {
        double totalTarget = 0;
        double totalDone = 0;
        for (MuscleStatsDto s : stats) {
            if (s.targetSets() == null || s.targetSets() == 0) {
                continue;
            }
            totalTarget += s.targetSets();
            totalDone += Math.min(s.sets(), s.targetSets());
        }
        if (totalTarget == 0) {
            return null;
        }
        return (int) Math.round(totalDone * 100.0 / totalTarget);
    }

    @Transactional(readOnly = true)
    public List<MuscleStatsDto> getMuscleStats(LocalDate weekStart) {
        Plan plan = planRepository.findByActiveTrue().orElse(null);
        return buildMuscleStats(resolveWeek(weekStart), plan);
    }

    @Transactional(readOnly = true)
    public WeekSummaryDto getWeekSummary(LocalDate weekStart) {
        Week week = resolveWeek(weekStart);
        Plan plan = planRepository.findByActiveTrue().orElse(null);

        int workoutsDone = (int) workoutRepository.countFinishedBetween(week.from(), week.to());
        int workingSets = (int) workoutRepository.countWorkingSetsBetween(week.from(), week.to());
        Integer workoutsPlanned = plan != null ? plan.getDays().size() : null;
        Integer targetPercent = calculateTargetPercent(buildMuscleStats(week, plan));

        return statsMapper.toWeekSummaryDto(workoutsDone, workoutsPlanned, workingSets, targetPercent);
    }
}

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
import com.gymtracker.service.TimeWindows.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final TimeWindows timeWindows;

    private record MuscleLoad(double sets, double reps) {
        static final MuscleLoad NONE = new MuscleLoad(0, 0);

        MuscleLoad plus(MuscleLoad other) {
            return new MuscleLoad(sets + other.sets, reps + other.reps);
        }
    }

    private double coefficient(MuscleRole role) {
        return role == MuscleRole.primary ? 1.0 : SECONDARY_COEFFICIENT;
    }

    private Map<Short, MuscleLoad> loadByMuscle(Window window, boolean finishedOnly) {
        Map<Short, MuscleLoad> load = new HashMap<>();
        for (MuscleLoadRow row : workoutRepository.findMuscleLoad(window.from(), window.to(), finishedOnly)) {
            double k = coefficient(row.role());
            load.merge(row.muscleId(), new MuscleLoad(row.sets() * k, row.reps() * k), MuscleLoad::plus);
        }
        return load;
    }

    private Map<Short, Short> weeklyTargets(Plan plan, List<Muscle> muscles) {
        Map<Short, Short> own = new HashMap<>();
        if (plan != null) {
            for (PlanMuscleTarget target : plan.getMuscleTargets()) {
                own.put(target.getId().getMuscleId(), target.getWeeklySets());
            }
        }
        // нет плана или общей цели: считаем по цели по умолчанию, чтобы карта и процент недели совпадали
        Short defaultTarget = plan != null && plan.getDefaultWeeklySets() != null
                ? plan.getDefaultWeeklySets() : DEFAULT_WEEKLY_SETS;

        Map<Short, Short> targets = new HashMap<>();
        for (Muscle m : muscles) {
            targets.put(m.getId(), own.getOrDefault(m.getId(), defaultTarget));
        }
        return targets;
    }

    private List<MuscleStatsDto> buildMuscleStats(Window week, Plan plan) {
        Map<Short, MuscleLoad> load = loadByMuscle(week, false);
        List<Muscle> muscles = muscleRepository.findAllByOrderByMuscleGroup_SortOrderAscSortOrderAsc();
        Map<Short, Short> targets = weeklyTargets(plan, muscles);

        return muscles.stream()
                .map(m -> {
                    MuscleLoad l = load.getOrDefault(m.getId(), MuscleLoad.NONE);
                    return statsMapper.toMuscleStatsDto(m, l.sets(), l.reps(), targets.get(m.getId()));
                })
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
        return buildMuscleStats(timeWindows.week(weekStart), plan);
    }

    @Transactional(readOnly = true)
    public WeekSummaryDto getWeekSummary(LocalDate weekStart) {
        Window week = timeWindows.week(weekStart);
        Plan plan = planRepository.findByActiveTrue().orElse(null);

        int workoutsDone = (int) workoutRepository.countFinishedBetween(week.from(), week.to());
        int workingSets = (int) workoutRepository.countWorkingSetsBetween(week.from(), week.to());
        Integer workoutsPlanned = plan != null ? plan.getDays().size() : null;
        Integer targetPercent = calculateTargetPercent(buildMuscleStats(week, plan));

        return statsMapper.toWeekSummaryDto(workoutsDone, workoutsPlanned, workingSets, targetPercent);
    }
}

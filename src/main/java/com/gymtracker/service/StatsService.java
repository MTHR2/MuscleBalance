package com.gymtracker.service;

import com.gymtracker.dto.stats.MonthlyWorkoutsDto;
import com.gymtracker.dto.stats.MuscleGroupStatsDto;
import com.gymtracker.dto.stats.MuscleLoadRow;
import com.gymtracker.dto.stats.MuscleStatsDto;
import com.gymtracker.dto.stats.StatsOverviewDto;
import com.gymtracker.dto.stats.StatsPeriod;
import com.gymtracker.dto.stats.WeekSummaryDto;
import com.gymtracker.entity.Muscle;
import com.gymtracker.entity.MuscleGroup;
import com.gymtracker.entity.MuscleRole;
import com.gymtracker.entity.Plan;
import com.gymtracker.entity.PlanMuscleTarget;
import com.gymtracker.mapper.StatsMapper;
import com.gymtracker.repository.MuscleRepository;
import com.gymtracker.repository.PlanRepository;
import com.gymtracker.repository.StatsRepository;
import com.gymtracker.repository.StatsRepository.PeriodCountRow;
import com.gymtracker.repository.WorkoutRepository;
import com.gymtracker.service.TimeWindows.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final double SECONDARY_COEFFICIENT = 0.5;
    private static final Short DEFAULT_WEEKLY_SETS = 10;

    private final StatsMapper statsMapper;
    private final WorkoutRepository workoutRepository;
    private final MuscleRepository muscleRepository;
    private final PlanRepository planRepository;
    private final StatsRepository statsRepository;
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

    @Transactional(readOnly = true)
    public StatsOverviewDto getOverview(StatsPeriod period) {
        Plan plan = planRepository.findByActiveTrue().orElse(null);

        YearMonth thisMonth = YearMonth.from(timeWindows.today());
        Window month = timeWindows.days(thisMonth.atDay(1), thisMonth.atEndOfMonth());
        int workoutsThisMonth = (int) workoutRepository.countFinishedBetween(month.from(), month.to());

        Window last30 = timeWindows.lastDays(30);
        Window prev30 = timeWindows.previous(last30);
        BigDecimal volume = workoutRepository.sumFinishedVolumeBetween(last30.from(), last30.to());
        BigDecimal prevVolume = workoutRepository.sumFinishedVolumeBetween(prev30.from(), prev30.to());

        Window window = timeWindows.period(period);
        long workouts = workoutRepository.countFinishedBetween(window.from(), window.to());
        long sets = workoutRepository.countFinishedWorkingSetsBetween(window.from(), window.to());
        Double avgDuration = statsRepository.avgDurationSeconds(window.from(), window.to());

        return new StatsOverviewDto(
                workoutsThisMonth,
                plannedInMonth(plan, thisMonth),
                weekStreak(plan),
                toTonnes(volume),
                toTonnes(prevVolume),
                changePercent(volume, prevVolume),
                avgDuration != null ? Math.round(avgDuration) : null,
                workouts > 0 ? Math.round(sets * 10.0 / workouts) / 10.0 : null);
    }

    @Transactional(readOnly = true)
    public List<MuscleGroupStatsDto> getMuscleGroupStats(StatsPeriod period) {
        Window window = timeWindows.period(period);
        Plan plan = planRepository.findByActiveTrue().orElse(null);
        Map<Short, MuscleLoad> load = loadByMuscle(window, true);
        List<Muscle> muscles = muscleRepository.findAllByOrderByMuscleGroup_SortOrderAscSortOrderAsc();
        Map<Short, Short> targets = weeklyTargets(plan, muscles);

        Map<MuscleGroup, List<Muscle>> byGroup = muscles.stream()
                .filter(m -> targets.get(m.getId()) > 0)
                .collect(Collectors.groupingBy(Muscle::getMuscleGroup, LinkedHashMap::new, Collectors.toList()));

        List<MuscleGroupStatsDto> result = new ArrayList<>();
        byGroup.forEach((group, groupMuscles) -> {
            double sets = groupMuscles.stream()
                    .mapToDouble(m -> load.getOrDefault(m.getId(), MuscleLoad.NONE).sets()).sum();
            int weeklyTarget = groupMuscles.stream().mapToInt(m -> targets.get(m.getId())).sum();
            result.add(new MuscleGroupStatsDto(group.getId(), group.getName(),
                    sets, (int) Math.round(weeklyTarget * window.weeks())));
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<MonthlyWorkoutsDto> getMonthlyWorkouts(int months) {
        Plan plan = planRepository.findByActiveTrue().orElse(null);
        YearMonth last = YearMonth.from(timeWindows.today());
        YearMonth first = last.minusMonths(months - 1L);
        Window window = timeWindows.days(first.atDay(1), last.atEndOfMonth());

        Map<LocalDate, Long> counts = toMap(statsRepository.countFinishedByMonth(window.from(), window.to(),
                timeWindows.zoneId()));

        List<MonthlyWorkoutsDto> result = new ArrayList<>();
        for (YearMonth m = first; !m.isAfter(last); m = m.plusMonths(1)) {
            int count = counts.getOrDefault(m.atDay(1), 0L).intValue();
            result.add(new MonthlyWorkoutsDto(m, count, plannedInMonth(plan, m)));
        }
        return result;
    }

    private Integer plannedInMonth(Plan plan, YearMonth month) {
        if (plan == null) {
            return null;
        }
        Set<Integer> trainingDays = plan.getDays().stream()
                .map(d -> d.getDayOfWeek().intValue())
                .collect(Collectors.toSet());
        int planned = 0;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            if (trainingDays.contains(month.atDay(day).getDayOfWeek().getValue())) {
                planned++;
            }
        }
        return planned;
    }

    private Integer weekStreak(Plan plan) {
        int required = plan != null && !plan.getDays().isEmpty() ? plan.getDays().size() : 1;
        Map<LocalDate, Long> perWeek = toMap(statsRepository.countFinishedByWeek(timeWindows.zoneId()));

        LocalDate currentWeek = timeWindows.week(null).firstDay();
        int streak = 0;
        for (LocalDate week = currentWeek.minusWeeks(1);
             perWeek.getOrDefault(week, 0L) >= required;
             week = week.minusWeeks(1)) {
            streak++;
        }
        if (perWeek.getOrDefault(currentWeek, 0L) >= required) {
            streak++;
        }
        return streak;
    }

    private Map<LocalDate, Long> toMap(List<PeriodCountRow> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (PeriodCountRow row : rows) {
            map.put(row.getPeriodStart(), row.getCount());
        }
        return map;
    }

    private double toTonnes(BigDecimal kg) {
        return kg.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private Integer changePercent(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) {
            return null;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
